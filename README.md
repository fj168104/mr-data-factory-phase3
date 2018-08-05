# mr-data-factory-phase3

#第三期爬取相关说明

##（一）相关表：爬网原始数据及中间数据
```
create table `scrapy_data`(
  `id` INTEGER NOT NULL AUTO_INCREMENT,
  `url` VARCHAR(1023) NOT NULL COMMENT 'url',
  `source` VARCHAR(100) NOT NULL COMMENT '数据来源{工信部，商务部，海关}',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '本条记录创建时间',
  `hash_key` VARCHAR(40) NOT NULL COMMENT 'url的md5结果（如有附件，则保存在此目录中）',
  `attachment_type` VARCHAR(40) NULL COMMENT '附件类型（pdf,doc,xls,jpg,tiff...）',
  `html` MEDIUMTEXT COMMENT '正文html',
  `text` MEDIUMTEXT COMMENT '正文text，提取到的正文',
  `fields` TEXT COMMENT '提取到的关键数据',
  PRIMARY KEY (`id`)
) COMMENT '爬网原始数据及中间数据';

```
##备注：
1、用于存储爬起的各个站点相关信息。

2、hash_kay用户存储目前站点网页的源文件html,附件文档等的相关信息的路径，最后一级目录就是由目标地址url通过md5加密获取，倒数第二级目录为类所在的包名称。文件名统一为标题名称

具体案例如：F:/home/fengjiang/Documents\mofcomsite\624301ffdbb80df309f3ac2c746484f3

3、此表的存储数据案例可以参考：data_factory_xu库中的记录

##（二）相关表：行政处罚表
```
CREATE TABLE `admin_punish` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '本条记录创建时间',
  `updated_at` timestamp NULL DEFAULT NULL COMMENT '本条记录最后更新时间',
  `source` varchar(100) NOT NULL COMMENT '数据来源',
  `subject` varchar(100) NOT NULL COMMENT '主题',
  `unique_key` varchar(767) NOT NULL COMMENT '唯一性标识(同一数据来源的同一主题内唯一)',
  `url` varchar(255) NOT NULL COMMENT 'url',
  `object_type` varchar(2) NOT NULL COMMENT '主体类型: 01-企业 02-个人',
  `enterprise_name` varchar(100) DEFAULT NULL COMMENT '企业名称',
  `enterprise_code1` varchar(30) DEFAULT NULL COMMENT '统一社会信用代码',
  `enterprise_code2` varchar(30) DEFAULT NULL COMMENT '营业执照注册号',
  `enterprise_code3` varchar(30) DEFAULT NULL COMMENT '组织机构代码',
  `enterprise_code4` varchar(30) DEFAULT NULL COMMENT '税务登记号',
  `person_name` varchar(100) DEFAULT NULL COMMENT '法定代表人|负责人姓名',
  `person_id` varchar(30) DEFAULT NULL COMMENT '法定代表人身份证号|负责人身份证号',
  `punish_type` varchar(100) DEFAULT NULL COMMENT '处罚类型',
  `punish_reason` varchar(2048) DEFAULT NULL COMMENT '处罚事由',
  `punish_according` varchar(2048) DEFAULT NULL COMMENT '处罚依据',
  `punish_result` varchar(1024) DEFAULT NULL COMMENT '处罚结果',
  `judge_no` varchar(100) DEFAULT NULL COMMENT '执行文号',
  `judge_date` varchar(30) DEFAULT NULL COMMENT '执行时间',
  `judge_auth` varchar(100) DEFAULT NULL COMMENT '判决机关',
  `publish_date` varchar(30) DEFAULT NULL COMMENT '发布日期',
  `status` varchar(20) DEFAULT NULL COMMENT '当前状态',
  PRIMARY KEY (`id`),
  UNIQUE KEY `source` (`source`,`subject`,`unique_key`)
) ENGINE=InnoDB AUTO_INCREMENT=7609 DEFAULT CHARSET=utf8 COMMENT='行政处罚'
```
1、这张表的用法与二期一致

2.唯一标识（unique_key）目前暂且约定为url+企业名称/自然人名称+发布时间+发布机构

##（三）本期优先提取的关键内容包括
1.url目标页面地址

2.目标页面标题

3.处罚信息发布时间/处罚时间

4.处罚信息发布机构

5.信息发布文号

注：这些属性有的就提取，没有的就不考虑，其他的属性可以考虑下一期来做

##（四）工具类参考
1.工具操作类：SiteTaskExtend_CollgationSite类

2.业务操作类：MOFCOM_SXBG类

##（五）难点
1.网页格式不统一，比较凌乱

2.附件种类繁多

3.扫描件图片OCR

4.office附件等

##（六）其他