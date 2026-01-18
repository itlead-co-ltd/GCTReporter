package com.gct.reportgenerator.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) 配置类
 * 
 * @author GCT Team
 * @since 1.0.0
 */
@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("GCT Report Generator API")
                .version("1.0.0")
                .description("GCT Reporter - 程序员报表生成工具API文档\n\n" +
                    "提供报表的创建、查询、执行和导出等功能的RESTful API接口。\n\n" +
                    "**主要功能模块**:\n" +
                    "- 用户认证与授权 (Authentication & Authorization)\n" +
                    "- 报表管理 (Report Management)\n" +
                    "- 查询执行 (Query Execution)\n" +
                    "- Excel导出 (Excel Export)\n" +
                    "- 权限控制 (Access Control)")
                .contact(new Contact()
                    .name("GCT Team")
                    .email("support@gctreporter.com")
                    .url("https://github.com/gct-reporter"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080")
                    .description("本地开发环境"),
                new Server()
                    .url("http://dev.gctreporter.com")
                    .description("开发环境"),
                new Server()
                    .url("https://gctreporter.com")
                    .description("生产环境")
            ));
    }
}
