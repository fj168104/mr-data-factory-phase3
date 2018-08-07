package com.mr.modules.api.model;

import com.mr.common.base.model.BaseEntity;
import lombok.Data;

import java.util.Date;
import javax.persistence.*;

@Table(name = "production_quality")
@Data
public class ProductionQuality extends BaseEntity {
    /*@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;*/

    /**
     * 本条记录创建时间
     */
    //@Column(name = "created_at")
    private Date createdAt;

    /**
     * 本条记录最后更新时间
     */
    //@Column(name = "updated_at")
    private Date updatedAt;

    /**
     * url
     */
    private String url;

    /**
     * 数据来源{工信部，商务部}
     */
    private String source;

    /**
     * 企业名称
     */
    //@Column(name = "enterprise_name")
    private String enterpriseName;

    /**
     * 统一社会信用代码
     */
    //@Column(name = "enterprise_code1")
    private String enterpriseCode1;

    /**
     * 营业执照注册号
     */
    //@Column(name = "enterprise_code2")
    private String enterpriseCode2;

    /**
     * 组织机构代码
     */
    //@Column(name = "enterprise_code3")
    private String enterpriseCode3;

    /**
     * 检查产品
     */
    //@Column(name = "oper_production")
    private String operProduction;

    /**
     * 检查结果
     */
    //@Column(name = "oper_result")
    private String operResult;

    /**
     * 检查机关
     */
    //@Column(name = "oper_org")
    private String operOrg;

    /**
     * 发布日期
     */
    //@Column(name = "publish_date")
    private String publishDate;

}