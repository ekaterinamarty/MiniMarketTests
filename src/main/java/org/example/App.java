package org.example;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import org.example.db.dao.ProductsMapper;

import java.io.IOException;

public class App {
    public static void main(String[] args) throws IOException {
        var factory = new SqlSessionFactoryBuilder()
                .build(Resources.getResourceAsStream("myBatisConfig.xml"));

        try (var session = factory.openSession()) {
            var prod = session
                    .getMapper(ProductsMapper.class)
                    .selectByPrimaryKey(444L)
                    ;
            System.out.println(prod);
        }
    }
}
