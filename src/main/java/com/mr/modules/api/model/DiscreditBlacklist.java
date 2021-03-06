package com.mr.modules.api.model;

import java.util.Date;

import javax.persistence.Table;

import com.mr.common.base.model.BaseEntity;

@Table(name = "discredit_blacklist")
public class DiscreditBlacklist extends BaseEntity {
    /*@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
*/
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
     * 数据来源
     */
    private String source;

    /**
     * 主题
     */
    private String subject;

    /**
     * 唯一性标识(同一数据来源的同一主题内唯一)
     */
    private String uniqueKey="";


    /**
     * url
     */
    private String url;

    /**
     * 主体类型: 01-企业 02-个人
     */
    //@Column(name = "object_type")
    private String objectType;

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
     * 税务登记号
     */
    private String enterpriseCode4;

    /**
     * 法定代表人/负责人姓名|负责人姓名
     */
    //@Column(name = "person_name")
    private String personName;

    /**
     * 法定代表人身份证号|负责人身份证号
     */
    //@Column(name = "person_id")
    private String personId;

    /**
     * 失信类型
     */
    //@Column(name = "discredit_type")
    private String discreditType;

    /**
     * 失信行为
     */
    //@Column(name = "discredit_action")
    private String discreditAction;

    /**
     * 列入原因
     */
    //@Column(name = "punish_reason")
    private String punishReason;

    /**
     * 处罚结果
     */
    //@Column(name = "punish_result")
    private String punishResult;

    /**
     * 执行文号
     */
    //@Column(name = "judge_no")
    private String judgeNo;

    /**
     * 执行时间
     */
    //@Column(name = "judge_date")
    private String judgeDate;

    /**
     * 判决机关
     */
    //@Column(name = "judge_auth")
    private String judgeAuth;

    /**
     * 发布日期
     */
    //@Column(name = "publish_date")
    private String publishDate;

    /**
     * 当前状态
     */
    private String status;

    /**
     * @return id
     */
    /*public Integer getId() {
        return id;
    }*/

    /**
     * @param id
     */
    /*public void setId(Integer id) {
        this.id = id;
    }*/

    /**
     * 获取本条记录创建时间
     *
     * @return created_at - 本条记录创建时间
     */
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * 设置本条记录创建时间
     *
     * @param createdAt 本条记录创建时间
     */
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 获取本条记录最后更新时间
     *
     * @return updated_at - 本条记录最后更新时间
     */
    public Date getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 设置本条记录最后更新时间
     *
     * @param updatedAt 本条记录最后更新时间
     */
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * 获取数据来源
     *
     * @return source - 数据来源
     */
    public String getSource() {
        return source;
    }

    /**
     * 设置数据来源
     *
     * @param source 数据来源
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * 获取主题
     *
     * @return subject - 主题
     */
    public String getSubject() {
        return subject;
    }

    /**
     * 设置主题
     *
     * @param subject 主题
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    /**
     * 获取url
     *
     * @return url - url
     */
    public String getUrl() {
        return url;
    }

    /**
     * 设置url
     *
     * @param url url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 获取主体类型: 01-企业 02-个人
     *
     * @return object_type - 主体类型: 01-企业 02-个人
     */
    public String getObjectType() {
        return objectType;
    }

    /**
     * 设置主体类型: 01-企业 02-个人
     *
     * @param objectType 主体类型: 01-企业 02-个人
     */
    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    /**
     * 获取企业名称
     *
     * @return enterprise_name - 企业名称
     */
    public String getEnterpriseName() {
        return enterpriseName;
    }

    /**
     * 设置企业名称
     *
     * @param enterpriseName 企业名称
     */
    public void setEnterpriseName(String enterpriseName) {
        this.enterpriseName = enterpriseName;
    }

    /**
     * 获取统一社会信用代码
     *
     * @return enterprise_code1 - 统一社会信用代码
     */
    public String getEnterpriseCode1() {
        return enterpriseCode1;
    }

    /**
     * 设置统一社会信用代码
     *
     * @param enterpriseCode1 统一社会信用代码
     */
    public void setEnterpriseCode1(String enterpriseCode1) {
        this.enterpriseCode1 = enterpriseCode1;
    }

    /**
     * 获取营业执照注册号
     *
     * @return enterprise_code2 - 营业执照注册号
     */
    public String getEnterpriseCode2() {
        return enterpriseCode2;
    }

    /**
     * 设置营业执照注册号
     *
     * @param enterpriseCode2 营业执照注册号
     */
    public void setEnterpriseCode2(String enterpriseCode2) {
        this.enterpriseCode2 = enterpriseCode2;
    }

    /**
     * 获取组织机构代码
     *
     * @return enterprise_code3 - 组织机构代码
     */
    public String getEnterpriseCode3() {
        return enterpriseCode3;
    }

    /**
     * 设置组织机构代码
     *
     * @param enterpriseCode3 组织机构代码
     */
    public void setEnterpriseCode3(String enterpriseCode3) {
        this.enterpriseCode3 = enterpriseCode3;
    }
    
    public String getEnterpriseCode4() {
		return enterpriseCode4;
	}

	public void setEnterpriseCode4(String enterpriseCode4) {
		this.enterpriseCode4 = enterpriseCode4;
	}

	/**
     * 获取法定代表人/负责人姓名|负责人姓名
     *
     * @return person_name - 法定代表人/负责人姓名|负责人姓名
     */
    public String getPersonName() {
        return personName;
    }

    /**
     * 设置法定代表人/负责人姓名|负责人姓名
     *
     * @param personName 法定代表人/负责人姓名|负责人姓名
     */
    public void setPersonName(String personName) {
        this.personName = personName;
    }

    /**
     * 获取法定代表人身份证号|负责人身份证号
     *
     * @return person_id - 法定代表人身份证号|负责人身份证号
     */
    public String getPersonId() {
        return personId;
    }

    /**
     * 设置法定代表人身份证号|负责人身份证号
     *
     * @param personId 法定代表人身份证号|负责人身份证号
     */
    public void setPersonId(String personId) {
        this.personId = personId;
    }

    /**
     * 获取失信类型
     *
     * @return discredit_type - 失信类型
     */
    public String getDiscreditType() {
        return discreditType;
    }

    /**
     * 设置失信类型
     *
     * @param discreditType 失信类型
     */
    public void setDiscreditType(String discreditType) {
        this.discreditType = discreditType;
    }

    /**
     * 获取失信行为
     *
     * @return discredit_action - 失信行为
     */
    public String getDiscreditAction() {
        return discreditAction;
    }

    /**
     * 设置失信行为
     *
     * @param discreditAction 失信行为
     */
    public void setDiscreditAction(String discreditAction) {
        this.discreditAction = discreditAction;
    }

    /**
     * 获取列入原因
     *
     * @return punish_reason - 列入原因
     */
    public String getPunishReason() {
        return punishReason;
    }

    /**
     * 设置列入原因
     *
     * @param punishReason 列入原因
     */
    public void setPunishReason(String punishReason) {
        this.punishReason = punishReason;
    }

    /**
     * 获取处罚结果
     *
     * @return punish_result - 处罚结果
     */
    public String getPunishResult() {
        return punishResult;
    }

    /**
     * 设置处罚结果
     *
     * @param punishResult 处罚结果
     */
    public void setPunishResult(String punishResult) {
        this.punishResult = punishResult;
    }

    /**
     * 获取执行文号
     *
     * @return judge_no - 执行文号
     */
    public String getJudgeNo() {
        return judgeNo;
    }

    /**
     * 设置执行文号
     *
     * @param judgeNo 执行文号
     */
    public void setJudgeNo(String judgeNo) {
        this.judgeNo = judgeNo;
    }

    /**
     * 获取执行时间
     *
     * @return judge_date - 执行时间
     */
    public String getJudgeDate() {
        return judgeDate;
    }

    /**
     * 设置执行时间
     *
     * @param judgeDate 执行时间
     */
    public void setJudgeDate(String judgeDate) {
        this.judgeDate = judgeDate;
    }

    /**
     * 获取判决机关
     *
     * @return judge_auth - 判决机关
     */
    public String getJudgeAuth() {
        return judgeAuth;
    }

    /**
     * 设置判决机关
     *
     * @param judgeAuth 判决机关
     */
    public void setJudgeAuth(String judgeAuth) {
        this.judgeAuth = judgeAuth;
    }

    /**
     * 获取发布日期
     *
     * @return publish_date - 发布日期
     */
    public String getPublishDate() {
        return publishDate;
    }

    /**
     * 设置发布日期
     *
     * @param publishDate 发布日期
     */
    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    /**
     * 获取当前状态
     *
     * @return status - 当前状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置当前状态
     *
     * @param status 当前状态
     */
    public void setStatus(String status) {
        this.status = status;
    }
    
    
}