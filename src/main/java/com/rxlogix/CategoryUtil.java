package com.rxlogix;

import grails.util.Holders;
import oracle.jdbc.driver.OracleConnection;
import oracle.jdbc.pool.OracleDataSource;
import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;

import java.sql.*;
import java.util.List;

public class CategoryUtil {

    public static Boolean saveCategories(List<CategoryDTO> categoryList) throws Exception {
        Boolean result = false;
        OracleDataSource dataSource = new OracleDataSource();
        dataSource.setUser(Holders.getConfig().toProperties().getProperty("dataSources.pva.username"));
//        tODO
//        dataSource.setPassword(Holders.getConfig().toProperties().getProperty("dataSources.pva.password"));
        dataSource.setPassword(Holders.getConfig().toProperties().getProperty("dataSources.pva.password"));
        dataSource.setURL(Holders.getConfig().toProperties().getProperty("dataSources.pva.url"));
        dataSource.setImplicitCachingEnabled(true);
        dataSource.setFastConnectionFailoverEnabled(true);
        Connection connection = dataSource.getConnection();
        final String typeName = "TYP_OBJ_CAT_FACT";
        final String typeTableName = "TYP_TAB_CAT_FACT";
        Struct[] structs = new Struct[categoryList.size()];

        CategoryDTO cat = null;
        for (int i=0; i< categoryList.size();i++){

            cat = categoryList.get(i);
            structs[i] = connection.createStruct(typeName, new Object[]{
                    cat.getFactGrpId(),
                    cat.getCatId(), cat.getSubCatId(), cat.getCatName(), cat.getSubCatName(),
                    cat.getDmlType(),cat.getModule(), cat.getDataSource(),cat.getPrivateUserId(),
                    cat.getPriority(),cat.getCreatedBy(),cat.getCreatedDate(),cat.getUpdatedBy(),
                    cat.getUpdatedDate(),cat.getIsAutoTagged(),cat.getIsRetained(),cat.getUdNumber1(),
                    cat.getUdNumber2(), cat.getUdNumber3(), cat.getUdText1(), cat.getUdText2(),
                    cat.getUdText3(), cat.getUdText4(), cat.getUdDate1(), cat.getUdDate2(),cat.getFactGrpCol1(), cat.getFactGrpCol2(), cat.getFactGrpCol3(),
                    cat.getFactGrpCol4(),cat.getFactGrpCol5(),cat.getFactGrpCol6(),cat.getFactGrpCol7(),
                    cat.getFactGrpCol8(),cat.getFactGrpCol9(),cat.getFactGrpCol10()});
        }
        CallableStatement callableStatement = connection.prepareCall("{call pkg_category.P_CAT_FACT_INSERT(?)}");
        ArrayDescriptor arryDesc = ArrayDescriptor.createDescriptor(typeTableName, connection);
        Array oracleArray = new ARRAY(arryDesc, connection, structs);
        callableStatement.setArray(1, oracleArray);
        callableStatement.executeUpdate();
        result = true;
        return result;
    }

}
