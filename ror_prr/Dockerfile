FROM python:3.10.11-alpine3.17

RUN mkdir -p /app/build_dir/

WORKDIR /app/build_dir

COPY ./* /app/build_dir/

RUN apk add musl-dev gcc g++ && \
    pip install --upgrade pip && \ 
    pip install Cython==0.29.35 --no-cache-dir && \
    pip install numpy==1.24.3 --no-cache-dir --no-build-isolation && \
    pip install "versioneer[toml]"==0.28 && \
    pip install -r requirements.txt --no-cache-dir --no-deps --no-build-isolation && \
    python setup.py build_ext --inplace && \
    mv *.so /app/
WORKDIR /app 
RUN rm -rf ./build_dir
ENV LOG_DIR='logs_ror_prr'

CMD ["python" , "-c" ,"from ror_prr import app; app.run(port=6365 , host='0.0.0.0' ,threaded=False)"]

    