use TT;
create table invoice_seller(
                               oi_id varchar(30) not null comment '主键',
                               seller_name varchar(50) not null comment '销方名称',
                               seller_tax_no varchar(50) not null comment '销方税号',
                               seller_phone varchar(13) not null comment '销方电话',
                               seller_address varchar(100) not null comment '销方地址',
                               seller_branch_bank varchar(50) not null comment '销方开户行',
                               seller_bank_account BIGINT not null comment '销方开户行',
                               remark VARCHAR(200) comment '备注',
                               created_by varchar(20) not null,
                               created_time datetime not null default CURRENT_TIMESTAMP,
                               updated_by VARCHAR(20), updated_time datetime,
                               PRIMARY key (oi_id)
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4 comment '开票销方信息';

create unique index udx_invoice_seller_name on invoice_seller(seller_name);
create unique index udx_invoice_seller_taxno on invoice_seller(seller_tax_no);

insert into c_service(id, service_id, service_code, business_type_cd, `name`, seq, messageQueueName, is_instance, url, method, timeout, retry_count, provide_app_id, create_time, status_cd)
values(1867, '982024032389830038', 'invoice.saveInvoice', 'API', '开电子发票', 1, '', 'CMD', 'http://fee-service', 'POST', 60, 3, '8000418002', '2024-04-23 00:09:17', '0');

insert into c_route(app_id, service_id, order_type_cd, invoke_limit_times, invoke_model, create_time, status_cd)
values('8000418004', '982024032389830038', 'D', '1000', 'S', CURRENT_TIMESTAMP, '0');

insert into c_service(service_id, service_code, business_type_cd, `name`, seq, is_instance, url, method, timeout, retry_count, provide_app_id, create_time, status_cd)
values('982024032389830041', 'invoice.delInvoiceSellerById', 'API', '删除销方信息', 1, 'CMD', 'http://fee-service', 'POST', 60, 3, '8000418002', CURRENT_TIMESTAMP, '0');

insert into c_route(app_id, service_id, order_type_cd, invoke_limit_times, invoke_model, create_time, status_cd)
values('8000418004', '982024032389830041', 'D', 1000, 'S', CURRENT_TIMESTAMP, '0');

create table invoice_detail_setting(
                                       id VARCHAR(30) not null comment '主键',
                                       invoice_item_name varchar(50) not null comment '开票项名称',
                                       expense_name  VARCHAR(50) null comment '费用名称',
                                       expense_num VARCHAR(30) comment '费用编号',
                                       expense_rate DECIMAL(6,4) not null default 0.00,
                                       remark varchar(100) comment '备注',
                                       created_by VARCHAR(30) not null comment '创建人',
                                       created_time datetime not null comment '创建时间',
                                       updated_by varchar(30) comment '更新人',
                                       updated_time datetime comment '更新人',
                                       PRIMARY KEY (id),
                                       UNIQUE KEY (invoice_item_name)
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4 comment '开票明细设置表';

insert into c_service(service_id, service_code, business_type_cd, `name`, seq, is_instance, url, method, timeout, retry_count, provide_app_id, create_time, status_cd)
values('982024032389830042', 'invoice.saveDetailSetting', 'API', '保存开票明细设置数据', 1, 'CMD', 'http://fee-service', 'POST', 60, 3, '8000418002', CURRENT_TIMESTAMP, '0');

insert into c_route(app_id, service_id, order_type_cd, invoke_limit_times, invoke_model, create_time, status_cd)
values('8000418004', '982024032389830042', 'D', 1000, 'S', CURRENT_TIMESTAMP, '0');

insert into c_service(service_id, service_code, business_type_cd, `name`, seq, is_instance, url, method, timeout, retry_count, provide_app_id, create_time, status_cd)
values('982024032389830043', 'invoice.listDetailSetting', 'API', '查询开票明细设置数据', 1, 'CMD', 'http://fee-service', 'GET', 60, 3, '8000418002', CURRENT_TIMESTAMP, '0');

insert into c_route(app_id, service_id, order_type_cd, invoke_limit_times, invoke_model, create_time, status_cd)
values('8000418004', '982024032389830043', 'D', 1000, 'S', CURRENT_TIMESTAMP, '0');