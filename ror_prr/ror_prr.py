import time
import os
import gc
import sys
import json
import logging
import pandas as pd
import numpy as np
from multiprocessing import Pool
from functools import partial
import time ,shutil
from flask import Flask ,jsonify ,request
import json ,os ,sys
from timeit import default_timer as timer
from datetime import datetime

app = Flask(__name__)

logs_dir =os.environ.get("LOG_DIR")
if not logs_dir:
    raise Exception("Logs directory not found in Environment variable")
os.makedirs(logs_dir ,exist_ok=True)
log_file = "ROR_PRR_{:%Y-%m-%d}.logs".format(datetime.now())
logging.basicConfig(filename=os.path.join(logs_dir ,log_file) , level= logging.INFO , format="%(asctime)3s %(levelname)s: %(filename)3s- %(message)s")

logger = logging.getLogger(__name__)
default_error_message = "Inside ror_prr.py - An error occurred on line {}. Exception type: {}, Exception :{} occurred while processing."

def run_pool(func ,iterable ,cores=1):
    pool = Pool(cores)
    try:
        pool.map(func, iterable)
        pool.close()
        pool.join()
        del pool
        gc.collect()
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e
    
def get_cols_to_drop(df_cols , config):
    try:
        logger.info(f"Finding columns to drop from input dataset")
        common_cols = config["COMMON_COLS"]
        subgrp_cols = config["SUBGRP_COLUMNS"]
        for col in subgrp_cols:
            if col not in df_cols :
                err =f"Requested columns {col} for subgrp not in dataset received from Database"
                raise Exception(err)
        subgrp_cols.extend(common_cols)
        drop_cols = [_col for _col in df_cols if _col not in subgrp_cols]
        return drop_cols
    except Exception as e :
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e 
    
def delete_util(config ,paths, msg=""):
    try:
        if config["DELETE_INTERMEDIATE"].lower() == "true":
            if msg:
                logger.info(f"{msg}")

            if type(paths) != type([]):
                paths = [paths]

            for _path in paths:
                if _path != "" and os.path.exists(_path):
                    if os.path.isfile(_path):
                        os.remove(_path)
                    else:
                        shutil.rmtree(_path)
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e
    
class ExtractSubgroup:
    def extract_subgroup(self , subgrp_data_dir , config , db_data_files):
        '''
        subgrp_data_dir: directory in which subgroup csvs are stored
        data_files: Csv file to process
        '''
        try:
            part_no , db_data_file = db_data_files
            logger.info(f"Processing: {db_data_file.rsplit('/', 1)[1]}")
            df = pd.read_csv(db_data_file)
            df["BATCH_ID"].fillna(config["NONSUBGRP_TOKEN"], inplace=True)   
            grps = df.groupby("BATCH_ID", sort=False)
            for grp in grps.groups:
                _grp_data_dir = os.path.join(subgrp_data_dir, str(grp))
                retries = 5
                while (retries > 0):
                    retries = retries - 1
                    try:
                        os.makedirs(_grp_data_dir, exist_ok=True)
                        break
                    except Exception as ex:
                        logger.error(ex)
                        time.sleep(1)
                        logger.info(f"{retries}: retries left")
                _df = grps.get_group(grp)
                if grp !=config["NONSUBGRP_TOKEN"]:
                    drop_col = get_cols_to_drop(list(_df.columns) ,config)
                    _df.drop(drop_col , axis=1 ,inplace=True)
                _csv_file = os.path.join(_grp_data_dir, f"df_{0}.csv")
                if os.path.exists(_csv_file):
                    _df.to_csv(_csv_file, index=False, header=False, mode="a")
                else:
                    _df.to_csv(_csv_file, index=False)
         
            delete_util(config, [db_data_file] , msg="Deleting Db data files  ")
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e
        
    def process_csvs_into_preferedsize_chunks(self,config, des_subgrp_dir, subgrp_dir_to_process,prefered_chunksize=5000):
        try:
            csvs_to_process = os.listdir(subgrp_dir_to_process)
            # Apply sorting on csv-filenames especially for standard-subgroup cases
            csvs_to_process.sort(key=lambda x: int(x.split("_")[1].split(".")[0]))
            subgrp_name = subgrp_dir_to_process.rsplit("/", 1)[-1]
            des_subgrp_dir = os.path.join(des_subgrp_dir, subgrp_name)
            os.makedirs(des_subgrp_dir, exist_ok=True)
            part_no = 0
            capacity_remaining_per_part = prefered_chunksize
            write_head = True
            for _csv in csvs_to_process:
                _csv_path = os.path.join(subgrp_dir_to_process, _csv)
                df = pd.read_csv(_csv_path, chunksize=prefered_chunksize)
                for chunk in df:
                    des_csv_path = os.path.join(des_subgrp_dir, f"df_{part_no}.csv")
                    # excess represents excess size than capacity_remaining
                    excess = chunk.shape[0] - capacity_remaining_per_part
                    if excess == 0:
                        chunk.to_csv(des_csv_path, mode="a", index=False, header=write_head)
                        capacity_remaining_per_part = prefered_chunksize
                        part_no += 1
                        write_head = True
                    elif excess > 0:
                        chunk[:capacity_remaining_per_part].to_csv(des_csv_path,
                                                                   mode="a",
                                                                   index=False,
                                                                   header=write_head)
                        part_no += 1
                        write_head = True
                        des_csv_path = os.path.join(des_subgrp_dir, f"df_{part_no}.csv")
                        chunk[capacity_remaining_per_part:].to_csv(des_csv_path,
                                                                   mode="a",
                                                                   index=False,
                                                                   header=write_head)
                        capacity_remaining_per_part = prefered_chunksize - excess
                        write_head = False
                    else:
                        capacity_remaining_per_part = -excess
                        chunk.to_csv(des_csv_path, mode="a", index=False, header=write_head)
                        write_head = False
                delete_util(config ,_csv_path)
            # logger.info(f"{subgrp_name} is processed into prefered chunksize")
            delete_util(config ,subgrp_dir_to_process,msg="Deleting initial subgrp directories")
        except Exception as e:
            logger.info(f"Error processing: {os.path.join(subgrp_dir_to_process, _csv)} to preferred chunk-size.")
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e


    def subgrp_partition_multiprocessing(self, subgrp_dir ,db_data_dir,config ,main_data_dir  ):
        try:
            start_time = time.time()
            db_data_files = \
                    [(i, os.path.join(db_data_dir, f"df_{i}.csv")) for i in range(len(os.listdir(db_data_dir)))]
            func_subgrp =self.extract_subgroup
            _func_extract_subgrps = partial(func_subgrp , subgrp_dir ,config )
            run_pool(_func_extract_subgrps , db_data_files)
            subgrp_dirs_to_process = [os.path.join(subgrp_dir, folder) for folder in
                                            os.listdir(subgrp_dir)]
            mid_time = time.time()
            logger.info(f"Time taken in subgroup extraction is {(mid_time -start_time)/60} min")
            des_subgrp_dir = os.path.join(main_data_dir, "subgrp_chunks_dir")
            os.makedirs(des_subgrp_dir, exist_ok = True)
            func_process_csvs_into_chunks =self.process_csvs_into_preferedsize_chunks
            _func_process_csvs = partial(func_process_csvs_into_chunks,config,
                                            des_subgrp_dir)
            run_pool(_func_process_csvs ,subgrp_dirs_to_process ,cores=int(config["CORES"])) 
            return des_subgrp_dir
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e
        

class MergeSubgrp:

    def merge_ith_partition_multiprocessing(self,score_dir  ,des_dir, config):
        try:
            logger.info(f"Merge ith partition using multiprocessing")
            os.makedirs(des_dir)
            subgrp_names = os.listdir(score_dir)
            subgrp_names.sort()
            partitions = range(len(os.listdir(os.path.join(score_dir, subgrp_names[0]))))
            func_merge_ith = partial(self.merge_ith_partition, score_dir, subgrp_names,config, des_dir)
            try:
                run_pool(func_merge_ith, partitions ,int(config["CORES"]))
                logger.info(f"merge_ith_partition_multiprocessing is complete")
                delete_util(config , score_dir , msg="Deleting chunked subgrp directory")
            except Exception as e:
                raise e
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e
        
    def get_score_file_paths(score_dir):
        try:
            for root, _dir, files in os.walk(score_dir):
                for file in files:
                    _csv_path = os.path.join(root, file)
                    yield _csv_path
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e
    
    def merge_all_score_files(filepath_to_write, csvpaths_to_merge):
        try:
            with open(filepath_to_write, 'w') as f:
                header = True
                for csv_path in csvpaths_to_merge:
                    with open(csv_path, 'r') as _f:
                        if header:
                            f.writelines(_f.readlines())
                            header = False
                        else:
                            f.writelines(_f.readlines()[1:])
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e
        

    @staticmethod
    def merge_ith_partition(score_dir, subgrp_names, config,des_dir, part_no):
        try:
            logger.info(f"Merge ith partition")
            subgrpnames_readpaths = [(subgrp_name, os.path.join(score_dir, subgrp_name, f"df_{part_no}.csv"))
                                    for subgrp_name in subgrp_names]
            read_handles = [(subgrp_name, open(read_path, "r")) for subgrp_name, read_path in subgrpnames_readpaths]
            write_handle = open(os.path.join(des_dir, f"df_{part_no}.csv"), "w")
            header = None
            first = True

            count_of_common_colnames = 0 # represent common columns between all subgrp score partitions
            # build the header for column-wise merged ith partition
            for subgrp_name, handle in read_handles:
                _line = next(handle).strip().split(',')
                _subgrp = subgrp_name
                if first:
                    count_of_common_colnames = len(list(set(_line).intersection(set(config["COMMON_COLS"]))))
                    header = _line[:count_of_common_colnames]
                    first = False

                if _subgrp == config["NONSUBGRP_TOKEN"]:
                    header.extend([f"{__line}" for __line in _line[count_of_common_colnames:]])
                else:
                    header.extend([f"{__line}{_subgrp}" for __line in _line[count_of_common_colnames:] ]) 

            write_handle.write(",".join(val for val in header) + "\n")
            while True:
                try:
                    line = []
                    first = True
                    for _, handle in read_handles:
                        _line = next(handle).strip().split(',')
                        _line = _line if first else _line[count_of_common_colnames:]
                        first = False
                        line.extend(_line)
                    write_handle.write(",".join(val for val in line) + "\n")
                except StopIteration as ex:
                    logger.info(f"StopIteration is utilised.")
                    write_handle.close()
                    break
            for _, handle in read_handles:
                handle.close()
            logger.info(f"{part_no}th partition is merged and written")
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e
    @staticmethod 
    def merge_all_score_files(filepath_to_write, csvpaths_to_merge):
        try:
            with open(filepath_to_write, 'w') as f:
                header = True
                for csv_path in csvpaths_to_merge:
                    with open(csv_path, 'r') as _f:
                        if header:
                            f.writelines(_f.readlines())
                            header = False
                        else:
                            f.writelines(_f.readlines()[1:])
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e
    @staticmethod
    def get_score_file_paths(score_dir):
        try:
            for root, _dir, files in os.walk(score_dir):
                for file in files:
                    _csv_path = os.path.join(root, file)
                    yield _csv_path
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e
    def process_score_dir_for_db_upload(self ,score_dir, main_data_dir,exec_code ,config):
        try:
            logger.info(f"Preparing scores files for db upload")
            start = time.time()
            paths = self.get_score_file_paths(score_dir)
            all_score_file_merge_csv_path = os.path.join(main_data_dir,
                                                        f'{exec_code}_merged.csv')
            self.merge_all_score_files(all_score_file_merge_csv_path, paths)
            delete_util(config , score_dir , msg="Deleting unmerged csvs")
            logger.info(f"Time taken in preparing scores for upload is {(time.time()-start)/60} min")
            return all_score_file_merge_csv_path
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e

def get_next_n_items(df_iter, n, part_no):
    try:
        chunks = []
        row_counts = 0
        for df in df_iter:
            n -= 1
            row_counts += df.shape[0]
            chunks.append((part_no, df))
            part_no += 1
            if n == 0:
                break
        return chunks, len(chunks), row_counts
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e
import oracledb
from sqlalchemy import create_engine ,types
class Db_connection:
    
    def __init__(self ,connection_info):
        self.username =connection_info["username"]
        self.password = connection_info["password"]
        self.con_string = connection_info["url"]

    def create_read_meta_file(self, main_data_dir ,exec_code):
        try:
            engine = create_engine(f"oracle+oracledb://:@" ,connect_args={"user": self.username ,"password" :self.password ,"dsn" :self.con_string})
            en_con = engine.connect()

            query = f"select * from PVS_PRR_ROR_ML_INFO where execution_id={exec_code}"
            logger.info(f"Reading meta for {exec_code}")
            df_meta = pd.read_sql_query(query , en_con)
            df_meta.columns = [col.upper() for col in df_meta.columns]
            meta_filepath = os.path.join(main_data_dir , f"meta_{exec_code}.json")
            logger.info(f"Writing meta file , using first record , total number of PECs for {exec_code} in pvs_prr_ror_ml_info is {df_meta.shape[0]}")

            with open(meta_filepath , 'w') as f:
                f.write(df_meta.iloc[0].to_json())
            res= json.loads(df_meta.iloc[0].to_json())
            return res
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e
        
        finally:
            if "en_con" in locals():
                en_con.close()
            if "engine" in locals():
                engine.dispose()

    def read_table(self ,exec_code , table_name , db_data_dir ,config ,meta_info):
        try:
            os.makedirs(db_data_dir ,exist_ok=False)
            prefered_chunksize = int(config["CHUNKSIZE"])
            logger.info(f"Run Query to read data from DB table {table_name}")

            engine = create_engine(f"oracle+oracledb://:@" ,connect_args={"user": self.username ,"password" :self.password ,"dsn" :self.con_string})
            en_con = engine.connect()
            total_subgrps = int(meta_info["SUBGROUPS"]) +1 # 1 for non subgrp
            total_pecs = meta_info["TOTAL_PEC_COUNT"]
            pecs_to_read_each_time = prefered_chunksize//total_subgrps
            pecs_to_read_each_time = pecs_to_read_each_time if pecs_to_read_each_time>0 else 1
            start = 1
            i = 0
            _rows_readed = 0
            while start<=total_pecs:
                end = min(start + pecs_to_read_each_time - 1, total_pecs)
                query = f"select * from {table_name}_FULL_DATA_{exec_code} where PEC_RANK between {start} and {end}"
                df = pd.read_sql_query(query, en_con)
                df.columns = [col.upper() for col in df.columns]
                df.drop(columns="PEC_RANK", inplace=True)
                # check is any PEC missing sungroup data
                if df.shape[0] != ((end-start)+1) *total_subgrps:
                    raise Exception(f"Subgroup Data missing for PEC_RANK in between {start} and {end}")
                # check if common columns exists in input dataset 
                for col in config["COMMON_COLS"]:
                    if col not in df.columns:
                        raise Exception(f"error : {col} common column not in input dataset")
                df.sort_values(by=["BASE_ID", "MEDDRA_PT_CODE"], inplace=True)
                df["BATCH_ID"].fillna(config['NONSUBGRP_TOKEN'], inplace=True)
                df.to_csv(f"{db_data_dir}/df_{i}.csv", index=False)
                i += 1
                _rows_readed = _rows_readed + len(df)
                logger.info(f"Got {_rows_readed} rows till now.")
                start = start + pecs_to_read_each_time
            logger.info(f"DB read is complete.")

        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e
        
        finally:
            if "en_con" in locals():
                en_con.close()
            if "engine" in locals():
                engine.dispose()
    def call_procedure(self , proc ,params):
        try:
            engine = create_engine(f"oracle+oracledb://:@" ,connect_args={"user": self.username ,"password" :self.password ,"dsn" :self.con_string})
            
            conn =engine.raw_connection()
            cur= conn.cursor()
            cur.callproc(proc , params)
            cur.close()
            conn.commit()
            logger.info(f"procedure called")
        except Exception as e :
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e

        finally:
            if "conn" in locals():
                conn.close()
            if "engine" in locals():
                engine.dispose()
          
    def get_db_timestamp(self):
        try:
            engine = create_engine(f"oracle+oracledb://:@" ,connect_args={"user": self.username ,"password" :self.password ,"dsn" :self.con_string})
            en_con = engine.connect()


            _query = "with t as (select to_char(systimestamp,'TZH:TZM') as TIMEZONE from dual) " \
                     "select cast(cast(systimestamp as timestamp with time zone) at time zone TIMEZONE as timestamp) " \
                     "SYSTIMESTAMP from t"
            _df = pd.read_sql_query(_query, en_con)
            _df.columns = [col.upper() for col in _df.columns]
            start_timestamp = _df.iloc[0]["SYSTIMESTAMP"]
            return start_timestamp
        except Exception as e:
            logging.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e 
        finally:
            if "en_con" in locals():
                en_con.close()
            if "engine" in locals():
                engine.dispose()

    def create_write_table(self ,df ,table , index=False , operation="append" ,chunksize=100000):
        """
        write_table function in DBController class
        writing data to oracle table
        Args:
            df (_type_): DataFrame
            table (_type_): string
            index (bool, optional): Bool Defaults to False.
            operation (str, optional):string -> 'fail'/'append'/'replace'. Defaults to 'append'.
            chunksize (int, optional): number. Defaults to 100000.
        
        Raises:
            e: _description_
        """
        
        try:
            engine = create_engine(f"oracle+oracledb://:@" ,connect_args={"user": self.username ,"password" :self.password ,"dsn" :self.con_string})
            en_con = engine.connect()
            try:
                dtyp = {c:types.VARCHAR(int(np.nan_to_num(df.head(0)[c].str.len().max()))+10000) for c in df.columns[df.head(0).dtypes == 'object'].tolist()}
                df.head(0).to_sql(table.lower(), en_con, index=index, if_exists='fail', chunksize=chunksize, dtype=dtyp)
            except:
                pass

            dtyp = {c:types.VARCHAR(int(np.nan_to_num(df[c].str.len().max()))+10000) for c in df.columns[df.dtypes == 'object'].tolist()}
            df.to_sql(table.lower(), en_con, index=index, if_exists=operation, chunksize=chunksize, dtype=dtyp)
        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e
        finally:
            if "en_con" in locals():
                en_con.close()
            if "engine" in locals():
                engine.dispose()

    def write_scores_into_db(self, filepath_to_upload, table, meta_info, exec_code, config):
        try:
            start_timestamp = self.get_db_timestamp()
            _t1 = time.time()
            logger.info(f"Write _scores in DB")
            chunk_size = 20000
            # multiple = 3003/((int(meta_info["SUBGROUPS"])+1)*10)
            # chunk_size = min(int(chunk_size * multiple),10000)
            logger.info(f"Using chunksize = {chunk_size} for upload")
            df_iter = pd.read_csv(filepath_to_upload, dtype = object ,chunksize=chunk_size)
        
            table_name = f"PVS_{table}_OUTPUT_{exec_code}"
            func = partial(self.write_into_db ,table_name ,config )
            n_cores = int(config["CORES"])
            part_no = 0
            row_counts = 0
            while True:
                chunks, c_count, _row_counts = get_next_n_items(df_iter, n_cores, part_no)
                if c_count == 0:
                    break
                row_counts+=_row_counts
                part_no += c_count
                pool = Pool(c_count)
                _ = pool.map(func, chunks, chunksize=1)
                pool.close()
                pool.join()

            _time_delta = time.time() - _t1
            end_timestamp = start_timestamp + pd.Timedelta(f"{_time_delta} sec")
            self.call_procedure('p_pvs_logging', [start_timestamp, end_timestamp, exec_code, None,
                                                       f'{table_name} table created & populated',
                                                       None, None, row_counts])
        except Exception as e :
           logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
           raise e

    def write_into_db(self ,table_name,config ,df):
        try:
            logger.info(f"write_to_db")
            part_no, df = df
            _t1 = time.time()
            df_shape = df.shape
            df.drop(columns="BATCH_ID", inplace=True)
            df.fillna(config["NULL_TOKEN"], inplace=True)  # from config
            self.create_write_table(df , table_name ,chunksize=df_shape[0])
            logger.info(f"Upload part: {part_no} took {time.time() - _t1}sec.")

        except Exception as e:
            logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
            raise e

        
def is_db_empty(db_data_dir):
    """
    checks if no data was found in the db
    """
    try:
        is_empty = True if len(os.listdir(db_data_dir)) == 0 else False
        if not is_empty:
            df = pd.read_csv(os.path.join(db_data_dir, "df_0.csv"))
            if len(df)==0: # if csv with just a header is dumped.
                is_empty = True
        return is_empty
    except Exception as e:
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        raise e


@app.route('/RorPrrStatus' , methods =["GET"])
def status_check():
    logger.info("Status Check Request")
    return jsonify({"message": "application is running"}) , 200

@app.route("/getRorPrrSubgrps" ,methods=["POST"])
def get_ror_prr_subgrp():
    try:
        start = timer()
        data= request.get_json()
        table_name = data["score_type"]
        exec_code=data["executable_config_id"]
        data_source=data["data_source"]
        config = data["ml_config"]
        db_details = data["db_details"]
        db_details["url"] = db_details["url"].split("@")[1]

        config["SUBGRP_COLUMNS"] = data["subgrp_columns"] 
        if len(config["SUBGRP_COLUMNS"])==0:
            raise Exception("No subgrp column received in request")
        logger.info(f"Request received for exec_code : {exec_code} , data_source :{data_source} , score_type :{table_name} ,config : {config}")

        main_data_dir = os.path.join(config["TMP_DIR"] , f"main_{table_name}_{exec_code}")
        db_data_dir = os.path.join(main_data_dir,"db")
        os.makedirs(main_data_dir ,exist_ok=False)
        
        db = Db_connection(db_details)
        meta_info =db.create_read_meta_file(main_data_dir ,exec_code)
        
        db.read_table(exec_code , table_name , db_data_dir ,config ,meta_info)
        
        # db is empty 
        is_empty =is_db_empty(db_data_dir)
        if is_empty:
            logger.info(f"Empty DB for exec_id: {exec_code}, Terminating process.")
            return jsonify({"message" :  f"Empty dataset from DB for executable_config_id: {exec_code}"}) , 500
        
        if str(config["SAVE_INPUT"]).lower() == "true":
            des_dir = os.path.join(config["TMP_DIR"],f"{exec_code}_{table_name}_QUERY_DATA")
            shutil.copytree(db_data_dir ,des_dir)
            logger.info(f"Data rececived from DB saved")


        subgrp_dir = os.path.join(main_data_dir ,"subgrp_dir")
        os.makedirs(subgrp_dir ,exist_ok=False)

        extract_obj=ExtractSubgroup()
        chunk_subgrp_dir = extract_obj.subgrp_partition_multiprocessing(subgrp_dir ,db_data_dir, config ,main_data_dir)
        merge_dir = os.path.join(main_data_dir,f"merge_dir_{exec_code}" ,"ith_joined")
        merge_obj=MergeSubgrp()
        merge_obj.merge_ith_partition_multiprocessing(chunk_subgrp_dir ,merge_dir ,config)
        filepath_to_upload = merge_obj.process_score_dir_for_db_upload(merge_dir , main_data_dir , exec_code , config)
        db.write_scores_into_db(filepath_to_upload, table_name, meta_info, exec_code, config)


        logger.info(f"Time taken to complete the process is {(timer() - start) / 60} min")
        delete_util(config , [main_data_dir] , msg="Deleting all alert data files")

        return jsonify({"message": f"Successfully transformed data for {table_name} scores"}) , 200
	
    except KeyError as e:
        logger.info(f"Wrong parameters received \n ENDING PROCESS")
        logger.error(default_error_message.format(sys.exc_info()[-1].tb_lineno, type(e), e))
        return jsonify({"message":f"Wrong parameters received , {e}"}) , 500
    except Exception as e:
        logger.error(default_error_message.format( sys.exc_info()[-1].tb_lineno, type(e), e))
        delete_util(config , [main_data_dir] , msg="Exception : deleting all alert data files")
        return jsonify({"message":f"Exception : {e}"}) , 500
    finally:
        logger.info(f"Finally block ")

    

if __name__ == '__main__':
    app.run(debug=False , port=6365 , host="0.0.0.0" ,threaded=False)


    