package com.mr.common.util;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mr.framework.json.XML;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;


public class JsonUtil {
  public static Logger logger = (Logger) LoggerFactory.getLogger(JsonUtil.class);
  public static ObjectMapper mapper = new ObjectMapper().configure(Feature.ALLOW_SINGLE_QUOTES, true)
      .configure(Feature.ALLOW_COMMENTS, true).configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
      .configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);

  public static JsonNode getJson(String sJson) throws Exception {
    if (CommonUtil.isNull(sJson)) {
      return null;
    }
    return mapper.readTree(sJson);
  }


  public static JsonNode getJson(File file) throws Exception {
    return mapper.readTree(file); // mapper：ObjectMapper对象
  }

  public static String getJsonStringValue(JsonNode mainNode, String sPath) throws Exception {
    return getJsonStringValue(mainNode, sPath, "");
  }

  public static String getJsonStringValue(String mainNode, String sPath) throws Exception {
    return getJsonStringValue(mainNode, sPath, "");
  }

  public static String getJsonStringValue(JsonNode mainNode, String path, String defaultValue) throws Exception {
    //if (CommonUtil.isNullJson(mainNode)) {
    if (CommonUtil.ifIsEmpty(mainNode)) {
      return defaultValue;
    }
    JsonNode jNode = queryJson(mainNode, path);
    //if (CommonUtil.isNotNullJson(jNode)) {
    if (CommonUtil.ifIsNotEmpty(jNode)) {
      return jNode.asText();
    } else {
      return defaultValue;
    }
  }

  /**
   * 获取jsonnode中的指定值，若不存在或不是String则采用默认值
   * 
   * @param mainNode
   * @param sPath
   * @param sDefaultValue
   * @return
   * @throws Exception
   */
  public static String getJsonStringValue(String mainNode, String sPath, String sDefaultValue) throws Exception {
    return getJsonStringValue(JsonUtil.getJson(mainNode), sPath, sDefaultValue);
  }

  public static String getJsonNodeStringValue(JsonNode mainNode, String sPath) throws Exception {
    JsonNode node = mainNode.get(sPath);
    // System.out.println("node="+node);
    String sReturn = "";
    if (node != null) {
      sReturn = node.asText();
    }
    return sReturn;
  }

  public static double getJsonNodeDoubleValue(JsonNode mainNode, String sPath) throws Exception {
    JsonNode node = mainNode.get(sPath);
    // System.out.println("node="+node);
    double sReturn = 0;
    if (node != null) {
      sReturn = node.asDouble();
    }

    return sReturn;
  }

  public static int getJsonNodeIntValue(JsonNode mainNode, String sPath) throws Exception {
    JsonNode node = queryJson(mainNode, sPath);
    // System.out.println("node="+node);
    int sReturn = 0;
    if (node != null) {
      sReturn = node.asInt();
    }

    return sReturn;
  }

  public static void setJsonStringValue(String mainNode, String sPath, String sName, String sValue) throws Exception {
    setJsonStringValue(getJson(mainNode), sPath, sName, sValue);
  }

  public static void setJsonStringValue(JsonNode mainNode, String sPath, String sName, String sValue) throws Exception {
    JsonNode node = queryJsonForce(mainNode, sPath);
    ((ObjectNode) node).put(sName, sValue);
  }

  public static void setJsonStringValue(JsonNode mainNode, String sPath, String sValue) throws Exception {
    String[] aPath = CommonUtil.getPathArray(sPath);
    JsonNode node = queryJsonForce(mainNode, aPath[0]);
    ((ObjectNode) node).put(aPath[1], sValue);
  }

  public static void setJsonValue(JsonNode mainNode, String sPath, String sName, String sValue) throws Exception {
    setJsonValue(mainNode, sPath, sName, getJson(sValue));
  }

  public static void setJsonValue(JsonNode mainNode, String sPath, String sName, JsonNode sValue) throws Exception {
    JsonNode node = queryJsonForce(mainNode, sPath);
    ((ObjectNode) node).set(sName, sValue);
  }

  public static double getJsonDoubleValue(JsonNode mainNode, String sPath) throws Exception {
    JsonNode node = queryJson(mainNode, sPath);
    double sReturn = 0;
    if (node != null) {
      sReturn = node.asDouble();
    }

    return sReturn;
  }

  public static double getJsonDoubleValue(String mainNode, String sPath) throws Exception {
    return getJsonDoubleValue(getJson(mainNode), sPath);
  }

  public static int getJsonIntValue(String mainNode, String sPath) throws Exception {
    return getJsonIntValue(mainNode, sPath, 0);
  }

  public static int getJsonIntValue(JsonNode mainNode, String sPath) throws Exception {
    return getJsonIntValue(mainNode.toString(), sPath, 0);
  }

  public static int getJsonIntValue(JsonNode mainNode, String sPath, int iDefault) throws Exception {
    return getJsonIntValue(mainNode.toString(), sPath, iDefault);
  }

  public static int getJsonIntValue(String mainNode, String sPath, int iDefault) throws Exception {
    JsonNode node = queryJson(mainNode, sPath);
    if (node != null) {
      return node.asInt();
    } else {
      return iDefault;
    }
  }

  public static long getJsonLongValue(JsonNode mainNode, String sPath) throws Exception {
    JsonNode node = queryJson(mainNode, sPath);
    long sReturn = 0;
    if (node != null) {
      sReturn = node.asLong();
    }
    return sReturn;
  }
  
  public static void setJsonIntValue(String mainNode, String sPath, int sValue) throws Exception {
    setJsonIntValue(JsonUtil.getJson(mainNode), sPath, sValue);
  }
  public static void setJsonIntValue(JsonNode mainNode, String sPath, int sValue) throws Exception {
    String[] aPath = CommonUtil.getPathArray(sPath);
    JsonNode node = queryJsonForce(mainNode, aPath[0]);
    ((ObjectNode) node).put(aPath[1], sValue);
  }
  
  public static void setJsonDoubleValue(String mainNode, String sPath, double sValue) throws Exception {
    setJsonDoubleValue(JsonUtil.getJson(mainNode), sPath, sValue);
  }
  
  public static void setJsonDoubleValue(JsonNode mainNode, String sPath, double sValue) throws Exception {
    String[] aPath = CommonUtil.getPathArray(sPath);
    JsonNode node = queryJsonForce(mainNode, aPath[0]);
    ((ObjectNode) node).put(aPath[1], sValue);
  }

  public static void setJsonIntValue(String mainNode, String sPath, String sName, int sValue) throws Exception {
    setJsonIntValue(getJson(mainNode), sPath, sName, sValue);
  }

  public static void setJsonIntValue(JsonNode mainNode, String sPath, String sName, int sValue) throws Exception {
    JsonNode node = queryJsonForce(mainNode, sPath);
    ((ObjectNode) node).put(sName, sValue);
  }

  public static void setJsonLongValue(JsonNode mainNode, String sPath, String sName, long lValue) throws Exception {
    JsonNode node = queryJsonForce(mainNode, sPath);
    ((ObjectNode) node).put(sName, lValue);
  }

  public static void setJsonDoubleValue(String mainNode, String sPath, String sName, double sValue) throws Exception {
    setJsonDoubleValue(getJson(mainNode), sPath, sName, sValue);
  }

  public static void setJsonDoubleValue(JsonNode mainNode, String sPath, String sName, double sValue) throws Exception {
    JsonNode node = queryJsonForce(mainNode, sPath);
    ((ObjectNode) node).put(sName, sValue);
  }

  public static void setJsonNodeDoubleValue(JsonNode mainNode, String sName, double sValue) throws Exception {
    ((ObjectNode) mainNode).put(sName, sValue);
  }

  public static void setJsonNodeStringValue(JsonNode mainNode, String sName, String sValue) throws Exception {
    ((ObjectNode) mainNode).put(sName, sValue);
  }

  public static void setJsonNodeIntValue(JsonNode mainNode, String sName, int sValue) throws Exception {
    ((ObjectNode) mainNode).put(sName, sValue);
  }

  public static JsonNode matchJson(JsonNode mainNode, String sMatch) throws Exception {
    JsonNode nNode = JsonUtil.getJson("{}");
    String[] a = StringUtils.split(sMatch, ",");
    for (String s : a) {
      JsonNode n = JsonUtil.queryJson(mainNode, s);
      if (n != null) {
        ((ObjectNode) nNode).put(s, n);
      }
    }
    return nNode;
  }

  public static JsonNode matchJson(JsonNode mainNode, String sMatch, String nMatch) throws Exception {
    JsonNode nNode = JsonUtil.getJson("{}");
    String[] a = StringUtils.split(sMatch, ",");
    for (String s : a) {
      JsonNode n = JsonUtil.queryJson(mainNode, s);
      if (n != null) {
        ((ObjectNode) nNode).put(s, n);
      }
    }
    a = StringUtils.split(nMatch, ",");
    for (String s : a) {
      JsonNode n = JsonUtil.queryJson(mainNode, s);
      if (n == null) {
        throw new Exception(s + " is null");
      } else if (!NumberUtils.isNumber(n.asText())) {
        throw new Exception(s + " is not number");
      } else {
        ((ObjectNode) nNode).put(s, n);
      }
    }
    return nNode;
  }

  public static JsonNode matchJson(JsonNode mainNode, String sMatch, String iMatch, String nMatch) throws Exception {
    JsonNode nNode = JsonUtil.getJson("{}");
    String[] a = StringUtils.split(sMatch, ",");
    for (String s : a) {
      JsonNode n = JsonUtil.queryJson(mainNode, s);
      if (n != null) {
        ((ObjectNode) nNode).put(s, n);
      }
    }
    a = StringUtils.split(iMatch, ",");
    for (String s : a) {
      JsonNode n = JsonUtil.queryJson(mainNode, s);
      if (n == null) {
        throw new Exception(s + " is null");
      } else if (!n.isInt()) {
        throw new Exception(s + " is not double");
      } else {
        ((ObjectNode) nNode).put(s, n);
      }
    }
    a = StringUtils.split(nMatch, ",");
    for (String s : a) {
      JsonNode n = JsonUtil.queryJson(mainNode, s);
      if (n == null) {
        throw new Exception(s + " is null");
      } else if (!n.isDouble()) {
        throw new Exception(s + " is not double");
      } else {
        ((ObjectNode) nNode).put(s, n);
      }
    }
    return nNode;
  }

  public static String checkJson(JsonNode mainNode, String sMatch, String iMatch, String nMatch) throws Exception {
    String sError = "";
    String[] a = StringUtils.split(sMatch, ",");
    for (String s : a) {
      JsonNode n = JsonUtil.queryJson(mainNode, s);
      if (CommonUtil.isNull(n)) {
        sError = s + " is null";
      }
    }
    a = StringUtils.split(iMatch, ",");
    for (String s : a) {
      JsonNode n = JsonUtil.queryJson(mainNode, s);
      if (n == null) {
        sError = s + " is null";
      } else if (!n.isInt()) {
        sError = s + " is not int";
      }
    }
    a = StringUtils.split(nMatch, ",");
    for (String s : a) {
      JsonNode n = JsonUtil.queryJson(mainNode, s);
      if (n == null) {
        sError = s + " is null";
      } else if (!n.isDouble()) {
        sError = s + " is not double";
      }
    }
    return sError;
  }

  public static String checkJson(JsonNode mainNode, String sMatch, String nMatch) throws Exception {
    String sError = "";
    String[] a = StringUtils.split(sMatch, ",");
    for (String s : a) {
      JsonNode n = JsonUtil.queryJson(mainNode, s);
      if (CommonUtil.isNull(n)) {
        sError = s + " is null";
      }
    }
    a = StringUtils.split(nMatch, ",");
    for (String s : a) {
      JsonNode n = JsonUtil.queryJson(mainNode, s);
      if (n == null) {
        sError = s + " is null";
      } else if (!NumberUtils.isNumber(n.asText())) {
        sError = s + " is not number";
      }
    }
    return sError;
  }

  public static JsonNode mergeJson(String mainNode, String updateNode) throws Exception {
    if (!CommonUtil.isNull(updateNode)) {
      return mergeJson(getJson(mainNode), getJson(updateNode));
    } else {
      return getJson(mainNode);
    }
  }

  public static JsonNode mergeJson(JsonNode mainNode, String updateNode) throws Exception {
    if (!CommonUtil.isNull(updateNode)) {
      return mergeJson(mainNode, getJson(updateNode));
    } else {
      return mainNode;
    }
  }

  public static JsonNode mergeJson(String mainNode, JsonNode updateNode) throws Exception {
    if (updateNode != null) {
      return mergeJson(getJson(mainNode), updateNode);
    } else {
      return getJson(mainNode);
    }
  }

  public static JsonNode mergeJson(JsonNode mainNode, JsonNode updateNode) {
    if (updateNode != null) {
      Iterator<String> fieldNames = updateNode.fieldNames();
      while (fieldNames.hasNext()) {
        String fieldName = fieldNames.next();
        JsonNode node = updateNode.get(fieldName);
        JsonNode jsonNode = mainNode.get(fieldName);
        if (jsonNode != null) {
          if (jsonNode.isObject()) {
            mergeJson(jsonNode, node);
          } else if (jsonNode.isArray()) {
            ArrayNode aNode1 = (ArrayNode) jsonNode;
            ArrayNode aNode2 = (ArrayNode) node;
            for (int i = 0; i < aNode2.size(); i++) {
              String s2 = "" + aNode2.get(i).get("i");
              boolean bFound = false;
              for (int j = 0; j < aNode1.size(); j++) {
                String s1 = "" + aNode1.get(j).get("i");
                // System.out.println("s1="+s1+" s2="+s2);
                if (s1.equals(s2)) {
                  mergeJson(aNode1.get(j), aNode2.get(i));
                  // aNode1.set(j, aNode2.get(i));
                  bFound = true;
                  break;
                }
              }
              if (!bFound) {
                // System.out.println(bFound);
                aNode1.add(aNode2.get(i));
              }
            }
          } else {
            if (mainNode instanceof ObjectNode) {
              ((ObjectNode) mainNode).set(fieldName, node);
            }
          }
        } else {
          ((ObjectNode) mainNode).set(fieldName, node);
        }
      }
    }

    return mainNode;
  }

  public static boolean copyAllJson(String mainNode, String sourcePath, String updateNode, String targetPath)
      throws Exception {
    return copyAllJson(getJson(mainNode), sourcePath, getJson(updateNode), targetPath);
  }

  public static boolean copyAllJson(JsonNode mainNode, String sourcePath, String updateNode, String targetPath)
      throws Exception {
    return copyAllJson(mainNode, sourcePath, getJson(updateNode), targetPath);
  }

  public static boolean copyAllJson(String mainNode, String sourcePath, JsonNode updateNode, String targetPath)
      throws Exception {
    return copyAllJson(getJson(mainNode), sourcePath, updateNode, targetPath);
  }

  public static boolean copyAllJson(JsonNode mainNode, String sourcePath, JsonNode updateNode, String targetPath)
      throws Exception {
    ArrayNode aNode1 = (ArrayNode) queryJson(mainNode, sourcePath);

    if (aNode1 != null) {
      ArrayNode aNode2 = (ArrayNode) queryJsonArrayForce(updateNode, targetPath);

      for (int i = 0; i < aNode1.size(); i++) {
        aNode2.add(aNode1.get(i));
      }
      return true;
    } else {
      return false;
    }
  }

  public static boolean appendJson(String mainNode, String sourcePath, String updateNode) throws Exception {
    return appendJson(getJson(mainNode), sourcePath, getJson(updateNode));
  }

  public static boolean appendJson(JsonNode mainNode, String sourcePath, String updateNode) throws Exception {
    return appendJson(mainNode, sourcePath, getJson(updateNode));
  }

  public static boolean appendJson(String mainNode, String sourcePath, JsonNode updateNode) throws Exception {
    return appendJson(getJson(mainNode), sourcePath, updateNode);
  }

  public static boolean appendJson(JsonNode mainNode, String sourcePath, JsonNode updateNode) throws Exception {
    ArrayNode aNode1 = (ArrayNode) queryJsonArrayForce(mainNode, sourcePath);

    if (aNode1 != null) {
      aNode1.add(updateNode);
      return true;
    } else {
      return false;
    }
  }

  public static boolean moveAllJsonArray(String mainNode, String sourcePath, String updateNode, String targetPath)
      throws Exception {
    return moveAllJsonArray(getJson(mainNode), sourcePath, getJson(updateNode), targetPath);
  }

  public static boolean moveAllJsonArray(JsonNode mainNode, String sourcePath, String updateNode, String targetPath)
      throws Exception {
    return moveAllJsonArray(mainNode, sourcePath, getJson(updateNode), targetPath);
  }

  public static boolean moveAllJsonArray(String mainNode, String sourcePath, JsonNode updateNode, String targetPath)
      throws Exception {
    return moveAllJsonArray(getJson(mainNode), sourcePath, updateNode, targetPath);
  }

  public static boolean moveAllJsonArray(JsonNode mainNode, String sourcePath, JsonNode updateNode, String targetPath)
      throws Exception {
    ArrayNode aNode1 = (ArrayNode) queryJson(mainNode, sourcePath);

    if (aNode1 != null) {
      ArrayNode aNode2 = (ArrayNode) queryJsonArrayForce(updateNode, targetPath);

      for (int i = 0; i < aNode1.size(); i++) {
        aNode2.add(aNode1.get(i));
      }
      aNode1.removeAll();
      return true;
    } else {
      return false;
    }
  }

  public static boolean moveJsonArrayById(String mainNode, String sourcePath, String sCondition, String updateNode,
      String targetPath) throws Exception {
    return moveJsonArrayById(getJson(mainNode), sourcePath, sCondition, getJson(updateNode), targetPath);
  }

  public static boolean moveJsonArrayById(JsonNode mainNode, String sourcePath, String sCondition, String updateNode,
      String targetPath) throws Exception {
    return moveJsonArrayById(mainNode, sourcePath, sCondition, getJson(updateNode), targetPath);
  }

  public static boolean moveJsonArrayById(String mainNode, String sourcePath, String sCondition, JsonNode updateNode,
      String targetPath) throws Exception {
    return moveJsonArrayById(getJson(mainNode), sourcePath, sCondition, updateNode, targetPath);
  }

  public static boolean moveJsonArrayById(JsonNode mainNode, String sourcePath, String sCondition, JsonNode updateNode,
      String targetPath) throws Exception {
    ArrayNode aNode1 = (ArrayNode) queryJson(mainNode, sourcePath);

    if (aNode1 != null) {
      ArrayNode aNode2 = (ArrayNode) queryJsonArrayForce(updateNode, targetPath);
      String s = "," + sCondition + ",";

      for (int i = 0; i < aNode1.size(); i++) {
        if (s.indexOf("," + aNode1.get(i).get("i").asText() + ",") > -1) {
          aNode2.add(aNode1.get(i));
          aNode1.remove(i);
          i--;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  public static boolean deleteJsonArray(String mainNode, String updateNode) throws Exception {
    return deleteJsonArray(getJson(mainNode), getJson(updateNode));
  }

  public static boolean deleteJsonArray(JsonNode mainNode, String updateNode) throws Exception {
    return deleteJsonArray(mainNode, getJson(updateNode));
  }

  public static boolean deleteJsonArray(JsonNode mainNode, JsonNode updateNode) {
    return deleteJsonArray(mainNode, updateNode, "i");
  }

  public static boolean deleteJsonArray(JsonNode mainNode, JsonNode updateNode, String sSeparator) {
    boolean bDelete = false;
    Iterator<String> fieldNames = updateNode.fieldNames();
    while (fieldNames.hasNext()) {
      String fieldName = fieldNames.next();
      JsonNode node = updateNode.get(fieldName);
      JsonNode jsonNode = mainNode.get(fieldName);
      // System.out.println("node"+node);
      if (jsonNode != null) {
        if (jsonNode.isObject()) {
          deleteJsonArray(jsonNode, node);
        } else if (jsonNode.isArray()) {
          ArrayNode aNode1 = (ArrayNode) jsonNode;
          ArrayNode aNode2 = (ArrayNode) node;
          for (int i = 0; i < aNode2.size(); i++) {
            String s2 = "" + aNode2.get(i).get(sSeparator);
            for (int j = 0; j < aNode1.size(); j++) {
              String s1 = "" + aNode1.get(j).get(sSeparator);
              // System.out.println("s1=" + s1 + " s2=" + s2);
              if (s1.equals(s2)) {
                aNode1.remove(j);
                bDelete = true;
                // aNode1.set(j, null);
                break;
              }
            }
          }
        }
      }
    }

    return bDelete;
  }

  // 查询Json节点属性字段，返回指定位置和大小的数据
  public static LinkedHashMap<String, JsonNode> queryJsonField(JsonNode node, String sPath, int iPosition, int iSize)
      throws Exception {
    Iterator<Entry<String, JsonNode>> it = queryJson(node, sPath).fields();
    int iIndex = 0;
    int iCount = 0;
    LinkedHashMap<String, JsonNode> list = new LinkedHashMap<String, JsonNode>();
    while (it.hasNext()) {
      iIndex++;
      if (iCount >= iSize) {
        break;
      }
      Entry<String, JsonNode> entry = it.next();
      if (iIndex < iPosition) {
        continue;
      }
      list.put(entry.getKey(), entry.getValue());
      iCount++;
    }

    return list;
  }

  // 强制查询，如果不存在则生成节点
  public static JsonNode queryJsonForce(String mainNode, String updateNode) throws Exception {
    return queryJson(getJson(mainNode), updateNode, true, false);
  }

  public static JsonNode queryJsonForce(JsonNode mainNode, String updateNode) throws Exception {
    return queryJson(mainNode, updateNode, true, false);
  }

  public static JsonNode queryJsonArrayForce(String mainNode, String updateNode) throws Exception {
    return queryJson(getJson(mainNode), updateNode, true, true);
  }

  public static JsonNode queryJsonArrayForce(JsonNode mainNode, String updateNode) throws Exception {
    return queryJson(mainNode, updateNode, true, true);
  }

  public static JsonNode queryJson(String mainNode, String updateNode) throws Exception {
    return queryJson(getJson(mainNode), updateNode, false, false);
  }

  public static JsonNode queryJsonArrayNode(ArrayNode nodes, String field, String value) throws Exception {
    for (JsonNode nField : nodes) {
      if (value.equals(JsonUtil.getJsonStringValue(nField, field))) {
        return nField;
      }
    }
    return null;
  }

  public static JsonNode queryJsonArrayNode(JsonNode[] nodes, String field, String value) throws Exception {
    for (JsonNode nField : nodes) {
      if (value.equals(JsonUtil.getJsonStringValue(nField, field))) {
        return nField;
      }
    }
    return null;
  }

  public static JsonNode queryJsonByField(JsonNode mainNode, String updateNode, String sField, String sValue)
      throws Exception {
    JsonNode nodes = JsonUtil.queryJson(mainNode, updateNode);
    if (nodes != null) {
      Iterator<Entry<String, JsonNode>> it = nodes.fields();
      while (it.hasNext()) {
        Entry<String, JsonNode> entry = it.next();
        JsonNode nField = entry.getValue();
        if (sValue.equals(JsonUtil.getJsonStringValue(nField, sField))) {
          return nField;
        }
      }
    }
    return null;
  }

  public static JsonNode queryJsonByField(ArrayNode mainNode, String sField, String sValue) throws Exception {
    JsonNode node = null;
    for (JsonNode node1 : mainNode) {
      if (sValue.equals(JsonUtil.getJsonStringValue(node1, sField))) {
        return node1;
      }
    }
    return node;
  }

  public static JsonNode queryJson(JsonNode mainNode, String updateNode) throws Exception {
    return queryJson(mainNode, updateNode, false, false);
  }

  public static JsonNode queryJson(JsonNode mainNode, String sPath, boolean bForce, boolean bArray) throws Exception {
    JsonNode node = mainNode;
    String[] a = StringUtils.split(sPath, ".");

    for (String s : a) {
      String i = CommonUtil.getBetweenString(s, "[", "]");
      if ("".equals(i)) {
        JsonNode node1 = node.get(s);
        // System.out.println(node1);
        if (node1 == null) {
          if (bForce) {
            JsonNode node2 = null;
            if (!bArray) {
              node2 = getJson("{}");
            } else {
              node2 = getJson("[]");
            }
            ((ObjectNode) node).set(s, node2);
            node = node2;
          } else {
            node = null;
          }
        } else {
          node = node1;
        }
      } else {
        String n = CommonUtil.getBeforeString(s, "[");
        // System.out.println("s="+s+" n="+n);
        ArrayNode aNode1 = (ArrayNode) node.get(n);
        // 循环判断条件,
        String[] aSearch = StringUtils.split(i, ",");
        // 符合的条件
        String sCondition = "";
        for (String sSearch : aSearch) {
          String k = CommonUtil.getBeforeString(sSearch, "=");
          String v = CommonUtil.getAfterString(sSearch, "=");
          sCondition += ",'" + k + "':'" + v + "'";
        }
        if (aNode1 != null) {
          node = null;
          // System.out.println("k="+k+" v="+v);
          boolean bFound = false;
          // 针对节点循环
          for (JsonNode node1 : aNode1) {
            boolean bMatch = true;
            // System.out.println(node1.get(k));
            // 匹配条件
            for (String sSearch : aSearch) {
              String k = CommonUtil.getBeforeString(sSearch, "=");
              String v = CommonUtil.getAfterString(sSearch, "=");
              // System.out.println(sSearch);
              if (!v.equals(getJsonNodeStringValue(node1, k))) {
                bMatch = false;
                break;
              }
            }
            if (bMatch) {
              node = node1;
              bFound = true;
              break;
            }
          }

          if (bForce) {
            if (!bFound) {
              node = getJson("{" + sCondition.substring(1) + "}");
              aNode1.add(node);
            }
          }
        } else {
          if (bForce) {
            JsonNode node2 = getJson("['" + sCondition.substring(1) + "']");

            node = node2;
          } else {
            node = null;
          }
        }
      }
      if (node == null) {
        break;
      }
    }
    return node;
  }

  public static void setJsonValueNew(JsonNode mainNode, String sPath, String sName, JsonNode nValue) throws Exception {
    JsonNode node = queryJsonNew(mainNode, sPath, true);
    ((ObjectNode) node).set(sName, nValue);
  }
  
  public static void setJsonStringValueNew(JsonNode mainNode, String sPath, String sValue) throws Exception {
    String[] aPath = CommonUtil.getPathArray(sPath);
    JsonNode node = queryJsonNew(mainNode, aPath[0], true);
    ((ObjectNode) node).put(aPath[1], sValue);
  }

  public static void setJsonDoubleValueNew(JsonNode mainNode, String sPath, double sValue) throws Exception {
    String[] aPath = CommonUtil.getPathArray(sPath);
    JsonNode node = queryJsonNew(mainNode, aPath[0], true);
    ((ObjectNode) node).put(aPath[1], sValue);
  }

  public static void setJsonIntValueNew(JsonNode mainNode, String sPath, int sValue) throws Exception {
    String[] aPath = CommonUtil.getPathArray(sPath);
    JsonNode node = queryJsonNew(mainNode, aPath[0], true);
    ((ObjectNode) node).put(aPath[1], sValue);
  }

  public static String getJsonStringValueNew(JsonNode mainNode, String sPath, String sValue) throws Exception {
    return getJsonStringValueNew(mainNode, sPath, sValue, "");
  }
  
  public static String getJsonStringValueNew(JsonNode mainNode, String sPath, String sValue, String defaultValue) throws Exception {
    String[] aPath = CommonUtil.getPathArray(sPath);
    if (CommonUtil.ifIsEmpty(mainNode)) {
      return defaultValue;
    }
    JsonNode node = queryJsonNew(mainNode, aPath[0], false);
    if (CommonUtil.ifIsNotEmpty(node)) {
      JsonNode n = node.get(aPath[1]);
      if (CommonUtil.ifIsNotEmpty(n)) {
        return n.asText();
      } else {
        return defaultValue;
      }
    } else {
      return defaultValue;
    }
  }

  public static double getJsonDoubleValueNew(JsonNode mainNode, String sPath, double sValue) throws Exception {
    return getJsonDoubleValueNew(mainNode, sPath, sValue, 0.0);
  }
  
  public static double getJsonDoubleValueNew(JsonNode mainNode, String sPath, double sValue, double defaultValue) throws Exception {
    String[] aPath = CommonUtil.getPathArray(sPath);
    if (CommonUtil.ifIsEmpty(mainNode)) {
      return defaultValue;
    }
    JsonNode node = queryJsonNew(mainNode, aPath[0], false);
    if (CommonUtil.ifIsNotEmpty(node)) {
      JsonNode n = node.get(aPath[1]);
      if (CommonUtil.ifIsNotEmpty(n)) {
        return n.asDouble();
      } else {
        return defaultValue;
      }
    } else {
      return defaultValue;
    }
  }
  
  public static int getJsonIntValueNew(JsonNode mainNode, String sPath, int sValue) throws Exception {
    return getJsonIntValueNew(mainNode, sPath, sValue, 0);
  }
  
  public static int getJsonIntValueNew(JsonNode mainNode, String sPath, int sValue, int defaultValue) throws Exception {
    String[] aPath = CommonUtil.getPathArray(sPath);
    if (CommonUtil.ifIsEmpty(mainNode)) {
      return defaultValue;
    }
    JsonNode node = queryJsonNew(mainNode, aPath[0], false);
    if (CommonUtil.ifIsNotEmpty(node)) {
      JsonNode n = node.get(aPath[1]);
      if (CommonUtil.ifIsNotEmpty(n)) {
        return n.asInt();
      } else {
        return defaultValue;
      }
    } else {
      return defaultValue;
    }
  }

  /**
   * 支持父节点是数组的查找，例如：
   * a.b[0].c.d[1].f，如果数组下标对应的json不存在，则新增数组的json对象（可能位置与下标数字不一致）
   * @param mainNode  主json对象
   * @param sPath     查找路径
   * @param bForce    不存在节点时，是否新增
   * @return
   * @throws Exception
   */
  public static JsonNode queryJsonNew(JsonNode mainNode, String sPath, boolean bForce) throws Exception {
    return queryJsonNew(mainNode, sPath, bForce, false);
  }

  /**
   * 支持父节点是数组的查找，例如：
   * a.b[0].c.d[1].f，如果数组下标对应的json不存在，则新增数组的json对象（可能位置与下标数字不一致）
   * @param mainNode  主json对象
   * @param sPath     查找路径
   * @param bForce    不存在节点时，是否新增
   * @param bArray    查找节点是否数组
   * @return
   * @throws Exception
   */
  public static JsonNode queryJsonNew(JsonNode mainNode, String sPath, boolean bForce, boolean bArray) throws Exception {
    JsonNode node = mainNode;
    String[] a = StringUtils.split(sPath, ".");

    int iMax = a.length;
    for (int iCount = 0; iCount < iMax; iCount++) {
      String s = a[iCount];
      String i = CommonUtil.getBetweenString(s, "[", "]");
      if ("".equals(i)) {
        JsonNode node1 = node.get(s);
        // System.out.println(node1);
        if (node1 == null) {
          if (bForce) {
            JsonNode node2 = null;
            if (iCount == (iMax - 1)) {
              if (!bArray) {
                node2 = getJson("{}");
              } else {
                node2 = getJson("[]");
              }
            } else {
              node2 = getJson("{}");
            }
            ((ObjectNode) node).set(s, node2);
            node = node2;
          } else {
            node = null;
          }
        } else {
          node = node1;
        }
      } else {
        String n = CommonUtil.getBeforeString(s, "[");
        JsonNode node1 = node.get(n);
        if (node1 != null) {
          ArrayNode aNode1 = (ArrayNode) node1;
          JsonNode node2 = aNode1.get(Integer.valueOf(i));
          if (node2 == null) {
            if (bForce) {
              node2 = getJson("{}");
              aNode1.add(node2);
              node = node2;
            } else {
              node = null;
            }
          } else {
            node = node2;
          }
        } else {
          if (bForce) {
            ArrayNode node2 = (ArrayNode)getJson("[]");
            ((ObjectNode) node).set(n, node2);
            JsonNode node3 = getJson("{}");
            node2.add(node3);
            node = node3;
          } else {
            node = null;
          }
        }
      }
      if (node == null) {
        break;
      }
    }
    return node;
  }

  public static String getJsonString(JsonNode object, String sName, String sValue) throws Exception {
    String[] a = StringUtils.split(sName, ",");
    StringBuffer sb = new StringBuffer();
    for (String s : a) {
      JsonNode node = queryJson(object, s);
      if (node != null) {
        sb.append(",\"" + s + "\":" + node.toString());
      } else {
        sb.append(",\"" + s + "\":\"\"");
      }
    }
    if ("".equals(sName)) {
      if ("".equals(sValue)) {
        return object.toString();
      } else {
        return "{" + sValue + "," + object.toString().substring(1);
      }
    } else {
      if ("".equals(sValue)) {
        return "{" + sb.substring(1) + "}";
      } else {
        return "{" + sValue + sb + "}";
      }
    }
  }

  public static String getJsonString(JsonNode object, String sName) throws Exception {
    return getJsonString(object, sName, "");
  }

  public static String getJsonArrayString(JsonNode object, String sName, String sValue) throws Exception {
    String[] a = StringUtils.split(sName, ",");
    StringBuffer sb = new StringBuffer();
    for (String s : a) {
      JsonNode node = queryJson(object, s);
      if (node != null) {
        sb.append("," + node.toString());
      } else {
        sb.append(",\"\"");
      }
    }
    if ("".equals(sName)) {
      if ("".equals(sValue)) {
        return "[]";
      } else {
        return "[" + sValue + "]";
      }
    } else {
      if ("".equals(sValue)) {
        return "[" + sb.substring(1) + "]";
      } else {
        return "[" + sValue + sb + "]";
      }
    }
  }

  public static String getJsonArrayString(JsonNode object, String sName) throws Exception {
    return getJsonArrayString(object, sName, "");
  }

  // 获取Map的Json对象
  public static JsonNode getMapJson(Map<String, JsonNode> map, String sID) throws Exception {
    return map.get(sID);
  }

  // 判断是否json数组是否有传入元素的编号
  public static JsonNode getArrayJson(ArrayNode node, String sID) throws Exception {
    return getArrayJson(node, sID, "_id");
  }

  public static JsonNode getArrayJson(ArrayNode node, String sID, String sField) throws Exception {
    for (Iterator<JsonNode> iter = node.elements(); iter.hasNext();) {
      JsonNode circleNode = iter.next();
      if (sID.equals(circleNode.get(sField).asText())) {
        return circleNode;
      }
    }
    return null;
  }

  public static void sortJsonNode(JsonNode node, String sPath) throws Exception {
    JsonNode j = queryJson(node, sPath);
    if (j != null) {
      Iterator<Entry<String, JsonNode>> it = j.fields();
      SortedSet<String> keys = new TreeSet<String>();
      while (it.hasNext()) {
        Entry<String, JsonNode> entry = it.next();
        // list.add(entry.getKey());
        keys.add(entry.getKey());
      }

      JsonNode n = getJson("{}");
      ObjectNode no = (ObjectNode) n;
      Iterator<String> it1 = keys.iterator();
      while (it1.hasNext()) {
        String s = it1.next();
        no.set(s, j.get(s));
      }
      setJsonValue(node, "", sPath, n);
    }
  }

  public static boolean moveJsonNode(String mainNode, String sourcePath, String updateNode, String targetPath)
      throws Exception {
    return moveJsonNode(getJson(mainNode), sourcePath, getJson(updateNode), targetPath);
  }

  public static boolean moveJsonNode(JsonNode mainNode, String sourcePath, String updateNode, String targetPath)
      throws Exception {
    return moveJsonNode(mainNode, sourcePath, getJson(updateNode), targetPath);
  }

  public static boolean moveJsonNode(String mainNode, String sourcePath, JsonNode updateNode, String targetPath)
      throws Exception {
    return moveJsonNode(getJson(mainNode), sourcePath, updateNode, targetPath);
  }

  public static boolean moveJsonNode(JsonNode mainNode, String sourcePath, JsonNode updateNode, String targetPath)
      throws Exception {
    JsonNode aNode1 = queryJson(mainNode, sourcePath);
    int i = StringUtils.lastIndexOf(targetPath, ".");
    String s1 = "";
    String s2 = targetPath;
    if (i > -1) {
      s1 = targetPath.substring(0, i);
      s2 = targetPath.substring(i + 1);
    }
    setJsonValue(updateNode, s1, s2, aNode1);
    deleteJsonNode(mainNode, sourcePath);
    return true;
  }

  public static boolean deleteJsonNode(String mainNode, String sourcePath) throws Exception {
    return deleteJsonNode(getJson(mainNode), sourcePath);
  }

  public static boolean deleteJsonNode(JsonNode mainNode, String sourcePath) throws Exception {
    int i = StringUtils.lastIndexOf(sourcePath, ".");
    String s1 = "";
    String s2 = sourcePath;
    if (i > -1) {
      s1 = sourcePath.substring(0, i);
      s2 = sourcePath.substring(i + 1);
      ObjectNode n = ((ObjectNode) queryJson(mainNode, s1));
      if (n != null) {
        n.remove(s2);
      }
    } else {
      ((ObjectNode) mainNode).remove(sourcePath);
    }
    return true;
  }

  public static int substractJsonNodeIntValue(JsonNode mainNode, String sPath, String sNode) throws Exception {
    return addJsonNodeIntValue(mainNode, sPath, sNode, -1);
  }

  public static int addJsonNodeIntValue(JsonNode mainNode, String sPath, String sNode) throws Exception {
    return addJsonNodeIntValue(mainNode, sPath, sNode, 1);
  }

  public static int addJsonNodeIntValue(JsonNode mainNode, String sPath, String sNode, int iValue) throws Exception {
    JsonNode node = queryJsonForce(mainNode, sPath);
    int i = getJsonNodeIntValue(node, sNode);
    i += iValue;
    setJsonNodeIntValue(node, sNode, i);
    return i;
  }

  public static double addJsonNodeDoubleValue(JsonNode mainNode, String sPath, String sNode, double iValue)
      throws Exception {
    JsonNode node = queryJsonForce(mainNode, sPath);
    double i = getJsonNodeDoubleValue(node, sNode);
    i += iValue;
    setJsonNodeDoubleValue(node, sNode, i);
    return i;
  }

  /**
   * 获取对象的第一个key值
   * 
   * @param node
   * @return
   * @throws Exception
   */
  public static String queryObjectFirstKey(JsonNode node) throws Exception {
    Iterator<Entry<String, JsonNode>> it = node.fields();
    while (it.hasNext()) {
      Entry<String, JsonNode> entry = it.next();
      String key = entry.getKey();
      // System.out.println(key);
      return key;
    }
    return "";
  }

  /**
   * 获取对象的第一个keyObject
   * 
   * @param node
   * @return
   * @throws Exception
   */
  public static JsonNode queryObjectFirstKObject(JsonNode node) throws Exception {
    Iterator<Entry<String, JsonNode>> it = node.fields();
    while (it.hasNext()) {
      Entry<String, JsonNode> entry = it.next();
      String key = entry.getKey();
      JsonNode keyvalue = entry.getValue();
      // System.out.println(keyvalue);
      return getJson("{'k':'" + key + "','v':" + keyvalue + "}");
    }
    return null;
  }

  /**
   * 查询对象指定规则记录
   * 
   * @param node
   * @return
   * @throws Exception
   */
  public static String queryObjectRule(JsonNode node) throws Exception {
    JsonNode aNode = queryJson(node, "r");
    String r = getJsonValueWithPath(aNode);
    // for (int i = 0; i < aNode.size(); i++) {
    // r = "," + aNode.get(i).asText();
    // }
    // if (!isNull(r))
    // r = r.substring(1);
    return r;
  }

  public static String getJsonValueWithPath(JsonNode node) throws Exception {
    return getJsonValueWithPath(node, "");
  }

  // 返回基于key路径的对象
  public static String getJsonValueWithPath(JsonNode node, String sPrefix) throws Exception {
    List<String> list = new ArrayList<String>();
    genJsonQueryString(node, "", list, sPrefix);
    StringBuffer sb = new StringBuffer();
    for (String s : list) {
      sb.append("," + s);
    }
    if (!sb.toString().equals("")) {
      return "{" + sb.substring(1) + "}";
    } else {
      return "{}";
    }
  }

  public static void genJsonQueryString(JsonNode node, String sPath, List<String> list) throws Exception {
    genJsonQueryString(node, sPath, list, "");
  }

  // 返回基于对象key值的查询语句列表
  public static void genJsonQueryString(JsonNode node, String sPath, List<String> list, String sPrefix)
      throws Exception {
    // if(CommonUtil.ifIsNotEmpty(sPrefix)) sPrefix = sPrefix+".";
    if (node.isContainerNode()) {
      Iterator<Entry<String, JsonNode>> it = node.fields();
      if (!it.hasNext()) {
        list.add("'" + sPrefix + sPath.substring(1) + "':" + node);
      } else {
        while (it.hasNext()) {
          Entry<String, JsonNode> entry = it.next();
          genJsonQueryString(entry.getValue(), sPath + "." + entry.getKey(), list, sPrefix);
        }
      }
    } else {
      list.add("'" + sPrefix + sPath.substring(1) + "':" + node);
    }
  }

  /**
   * 获取jsonNode的字段名
   * 
   * @param jsonnode
   * @return
   */
  public static String getJsonStringName(JsonNode jsonnode) {
    return getJsonStringName(jsonnode, "");
  }

  /**
   * 获取jsonNode的字段名
   * 
   * @param jsonnode
   * @param sType
   *          字段名拼接方式
   *          <ul>
   *          <li>1: 以''包裹字段名作为查询条件格式</li>
   *          </ul>
   * @return
   */
  public static String getJsonStringName(JsonNode jsonnode, String sType) {
    if (jsonnode == null)
      return "";
    String sFieldNames = "";
    Iterator<String> it = jsonnode.fieldNames();
    while (it.hasNext()) {
      String sName = it.next();
      if ("1".equals(sType))
        sName = "'" + sName + "'";
      sFieldNames += "," + sName;
    }
    if (!CommonUtil.isNull(sFieldNames)) {
      sFieldNames = sFieldNames.substring(1);
    }
    return sFieldNames;
  }

  /**
   * 将jsonnode格式的数据格式化输出
   * 
   * @param input
   * @return {String}
   */
  public static String getJsonFormatPrint(String input) {
    String sPrint = "";
    try {
      Object json = mapper.readValue(input, Object.class);
      sPrint = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return sPrint;
  }

  public static JsonNode getJsonArrayByIndex(JsonNode jNode, int... aIndexs) throws Exception {
    if (CommonUtil.ifIsEmpty(jNode)) {
      logger.warn("[ERROR] result of getJsonArrayByIndex JsonNode is Empty! ", jNode);
      throw new Exception("[ERROR] result of getJsonArrayByIndex JsonNode is Empty");
    }
    if (CommonUtil.ifIsNotEmpty(jNode) && !jNode.isArray()) {
      logger.warn("[ERROR] result of getJsonArrayByIndex('{}') is not ArrayNode! ", jNode);
      throw new Exception("[ERROR] result of getJsonArrayByIndex('" + jNode + "') is not ArrayNode! ");
    }
    if (CommonUtil.ifIsEmpty(aIndexs)) {
      logger.warn("[ERROR] result of getJsonArrayByIndex('{}') is Empty Array! ", aIndexs);
      throw new Exception("[ERROR] result of getJsonArrayByIndex('{" + aIndexs + "}') is Empty Array! ");
    }
    JsonNode jReturn = JsonUtil.getJson("[]");
    for (int index : aIndexs) {
      ((ArrayNode) jReturn).add(((ArrayNode) jNode).get(index));
    }
    return jReturn;
  }

  public static JsonNode getJsonArrayByKeyValue(JsonNode jNode, String sKeyValues) throws Exception {
    JsonNode jKeyValues = null;
    if (CommonUtil.ifIsEmpty(sKeyValues)) {
      jKeyValues = JsonUtil.getJson("{}");
    } else {
      jKeyValues = JsonUtil.getJson(sKeyValues);
    }
    return getJsonArrayByKeyValue(jNode, jKeyValues);
  }

  public static JsonNode getJsonArrayByKeyValue(JsonNode jNode, JsonNode jKeyValues) throws Exception {
    if (CommonUtil.ifIsEmpty(jNode)) {
      logger.warn("[ERROR] result of getJsonArrayByIndex JsonNode is Empty! ", jNode);
      throw new Exception("[ERROR] result of getJsonArrayByIndex JsonNode is Empty");
    }
    if (CommonUtil.ifIsNotEmpty(jNode) && !jNode.isArray()) {
      logger.warn("[ERROR] result of getJsonArrayByIndex('{}') is not ArrayNode! ", jNode);
      throw new Exception("[ERROR] result of getJsonArrayByIndex('" + jNode + "') is not ArrayNode! ");
    }
    if (CommonUtil.ifIsEmpty(jKeyValues)) {
      return jNode;
    }
    JsonNode jReturn = JsonUtil.getJson("[]");
    Iterator<JsonNode> it = jNode.elements();
    while (it.hasNext()) {
      JsonNode node = it.next();
      Iterator<Entry<String, JsonNode>> it_kv = jKeyValues.fields();
      Boolean bMatch = true;
      while (it_kv.hasNext()) {
        Entry<String, JsonNode> entry = it_kv.next();
        String sKey = entry.getKey();
        JsonNode jValue = entry.getValue();
        if (CommonUtil.isNull(jValue))
          continue;
        String sValue = jValue.asText();
        if (!CommonUtil.ifIsIndexOf(sValue, JsonUtil.getJsonStringValue(node, sKey))) {
          bMatch = false;
          break;
        }
      }
      if (bMatch) {
        ((ArrayNode) jReturn).add(node);
      } else {
        continue;
      }
    }
    return jReturn;
  }

  public static void setJsonArrayValue(JsonNode jNode, String sPath, JsonNode arrayJson) throws Exception {
    if (CommonUtil.isNull(arrayJson)) {
      return;
    } else {
      JsonNode jPathValue = queryJson(jNode, sPath);
      if (CommonUtil.isNotNullJson(jPathValue) && !jPathValue.isArray()) {
        logger.warn("[WARN] result of queryJson('{}', '{}') is not Array! ", jNode, sPath);
        return;
      }
      if (CommonUtil.isNullJson(jPathValue)) {
        jPathValue = getJson("[]");
        String[] aPath = CommonUtil.getPathArray(sPath);
        JsonUtil.setJsonValue(jNode, aPath[0], aPath[1], jPathValue);
      }
      ArrayNode aPathValue = (ArrayNode) jPathValue;

      if (arrayJson.isArray()) {
        Iterator<JsonNode> it = arrayJson.elements();
        while (it.hasNext()) {
          JsonNode node = it.next();
          aPathValue.add(node);
        }
      } else if (arrayJson.isObject()) {
        aPathValue.add(arrayJson);
      }
    }
  }

  public static void setJsonArrayValue(JsonNode jNode, String sPath, String sArrayValue) throws Exception {
    JsonNode jPathValue = queryJson(jNode, sPath);
    if (CommonUtil.isNotNullJson(jPathValue) && !jPathValue.isArray()) {
      logger.warn("[WARN] result of queryJson('{}', '{}') is not Array! ", jNode, sPath);
      return;
    }
    if (CommonUtil.isNullJson(jPathValue)) {
      jPathValue = getJson("[]");
      String[] aPath = CommonUtil.getPathArray(sPath);
      JsonUtil.setJsonValue(jNode, aPath[0], aPath[1], jPathValue);
    }
    ArrayNode aPathValue = (ArrayNode) jPathValue;
    String[] aValue = StringUtils.split(sArrayValue, ",");
    for (String node : aValue) {
      aPathValue.add(node);
    }
  }

  /**
   * json信息拷贝
   * <ul>
   * <li>过期，优化调用遍历，建议采用copyJsonFields(JsonNode jFromJson, String sFields)</li>
   * </ul>
   * 
   * @param jFromJson
   *          源Json
   * @param jToJson
   *          目标Json
   * @param sFields
   *          拷贝信息项，逗号(,)分割
   * @throws Exception
   */
  @Deprecated
  public static void copyJsonFields(JsonNode jFromJson, JsonNode jToJson, String sFields) throws Exception {
    String[] aField = StringUtils.split(sFields, ",");
    for (String field : aField) {
      JsonNode jNode = JsonUtil.queryJson(jFromJson, field);
      JsonUtil.setJsonValue(jToJson, "", field, jNode);
    }
  }

  /**
   * json信息拷贝
   * <ul>
   * <li>过期，优化调用遍历，建议采用copyJsonFields(String sFromJson, String sFields)</li>
   * </ul>
   * 
   * @throws Exception
   */
  @Deprecated
  public static void copyJsonFields(String sFromJson, JsonNode jToJson, String sFields) throws Exception {
    String[] aField = StringUtils.split(sFields, ",");
    for (String field : aField) {
      JsonNode jNode = JsonUtil.queryJson(sFromJson, field);
      JsonUtil.setJsonValue(jToJson, "", field, jNode);
    }
  }

  /**
   * Json信息拷贝
   * <ul>
   * <li>针对同源(同key值)json信息拷贝</li>
   * </ul>
   * 
   * @param jFromJson
   *          源Json
   * @param sFields
   *          拷贝信息项，逗号(,)分割
   * @return {JsonNode}
   * @throws Exception
   */
  public static JsonNode copyJsonFields(JsonNode jFromJson, String sFields) throws Exception {
    return copyJsonFields(jFromJson.toString(), sFields);
  }

  /**
   * Json信息拷贝
   * <ul>
   * <li>针对同源(同key值)json信息拷贝</li>
   * </ul>
   * 
   * @param sFromJson
   *          源Json
   * @param sFields
   *          拷贝信息项，逗号(,)分割
   * @return {JsonNode}
   * @throws Exception
   */
  public static JsonNode copyJsonFields(String sFromJson, String sFields) throws Exception {
    String[] aField = StringUtils.split(sFields, ",");
    JsonNode jToJson = JsonUtil.getJson("{}");
    for (String field : aField) {
      JsonNode jNode = JsonUtil.queryJson(sFromJson, field);
      JsonUtil.setJsonValue(jToJson, "", field, jNode);
    }
    return jToJson;
  }

  /**
   * json信息拷贝
   * 
   * @param jFromJson
   *          源Json
   * @param jToJson
   *          目标Json
   * @param sFromFields
   *          拷贝信息项，逗号(,)分割
   * @param sToFields
   *          复制信息项，逗号(,)分割
   * @throws Exception
   */
  public static void copyJsonFields(JsonNode jFromJson, JsonNode jToJson, String sFromFields, String sToFields)
      throws Exception {
    String[] aFromFields = StringUtils.split(sFromFields, ",");
    String[] aToFields = StringUtils.split(sToFields, ",");
    int iLength = aFromFields.length;
    for (int i = 0; i < iLength; i++) {
      String sFromField = aFromFields[i];
      String sToField = aToFields[i];
      JsonNode jNode = JsonUtil.queryJson(jFromJson, sFromField);
      JsonUtil.setJsonValue(jToJson, "", sToField, jNode);
    }
  }

  /**
   * json信息拷贝
   * 
   * @param sFromJson
   *          源Json
   * @param jToJson
   *          目标Json
   * @param sFromFields
   *          拷贝信息项，逗号(,)分割
   * @param sToFields
   *          复制信息项，逗号(,)分割
   * @throws Exception
   */
  public static void copyJsonFields(String sFromJson, JsonNode jToJson, String sFromFields, String sToFields)
      throws Exception {
    String[] aFromFields = StringUtils.split(sFromFields, ",");
    String[] aToFields = StringUtils.split(sToFields, ",");
    int iLength = aFromFields.length;
    for (int i = 0; i < iLength; i++) {
      String sFromField = aFromFields[i];
      String sToField = aToFields[i];
      JsonNode jNode = JsonUtil.queryJson(sFromJson, sFromField);
      JsonUtil.setJsonValue(jToJson, "", sToField, jNode);
    }
  }

  public static Object toObject(JsonNode jInput) {
    if (CommonUtil.ifIsNull(jInput))
      return null;
    if (CommonUtil.ifIsEmpty(jInput))
      return "";
    if (jInput.isBoolean()) {
      return jInput.asBoolean();
    } else if (jInput.isDouble()) {
      return jInput.asDouble();
    } else if (jInput.isFloat()) {
      return jInput.asDouble();
    } else if (jInput.isInt()) {
      return jInput.asInt();
    } else if (jInput.isLong()) {
      return jInput.asLong();
    } else if (jInput.isTextual()) {
      return jInput.asText();
    } else {
      return jInput.toString();
    }
  }

  public static <T> void setValueOfJson(JsonNode jNode, String sPath, String sValue) throws Exception {
    setValueOfJson(jNode, sPath, "", sValue);
  }

  public static <T> void setValueOfJson(JsonNode jNode, String sFieldPath, String sFieldName, String sValue)
      throws Exception {
    JsonNode jValue = JsonUtil.getJson(sValue);
    setValue(jNode, sFieldPath, sFieldName, jValue);
  }

  public static <T> void setValue(JsonNode jNode, String sPath, T TValue) throws Exception {
    setValue(jNode, sPath, "", TValue);
  }

  public static <T> void setValue(JsonNode jNode, String sFieldPath, String sFieldName, T TValue) throws Exception {
    String sPath = "";
    // 方法参数校验
    if (CommonUtil.ifIsNotEmpty(sFieldPath) && CommonUtil.ifIsNotEmpty(sFieldName)) {
      sPath = sFieldPath + "." + sFieldName;
    } else if (CommonUtil.ifIsNotEmpty(sFieldPath)) {
      sPath = sFieldPath;
    } else if (CommonUtil.ifIsNotEmpty(sFieldName)) {
      sPath = sFieldName;
    } else {
      throw new Exception("[JsonUtil.setValue] 方法参数异常: 【设置的字段名为空】");
    }
    String[] aPath = CommonUtil.getPathArray(sPath);
    JsonNode node = null;
    if (CommonUtil.ifIsEmpty(aPath[0])) {
      node = jNode;
    } else {
      node = queryJsonForce(jNode, aPath[0]);
    }
    if (TValue instanceof BigDecimal) {
      ((ObjectNode) node).put(aPath[1], (BigDecimal) TValue);
    } else if (TValue instanceof Boolean) {
      ((ObjectNode) node).put(aPath[1], (Boolean) TValue);
    } else if (TValue instanceof byte[]) {
      ((ObjectNode) node).put(aPath[1], (byte[]) TValue);
    } else if (TValue instanceof Double) {
      ((ObjectNode) node).put(aPath[1], (Double) TValue);
    } else if (TValue instanceof Float) {
      ((ObjectNode) node).put(aPath[1], (Float) TValue);
    } else if (TValue instanceof Integer) {
      ((ObjectNode) node).put(aPath[1], (Integer) TValue);
    } else if (TValue instanceof Long) {
      ((ObjectNode) node).put(aPath[1], (Long) TValue);
    } else if (TValue instanceof Short) {
      ((ObjectNode) node).put(aPath[1], (Short) TValue);
    } else if (TValue instanceof String) {
      ((ObjectNode) node).put(aPath[1], (String) TValue);
    } else if (TValue instanceof JsonNode) {
      ((ObjectNode) node).set(aPath[1], (JsonNode) TValue);
    } else if (TValue.getClass().isArray()) {
      // addValue(jNode, sFieldPath, sFieldName, TValue);
      throw new Exception("[JsonUtil.setValue] 方法执行异常: 【设置数组内容请使用 addValue】");
    } else {
      ((ObjectNode) node).put(aPath[1], TValue.toString());
    }
  }

  public static <T> void addValue(JsonNode jNode, String sPath, T... TValue) throws Exception {
    addValue(jNode, sPath, "", TValue);
  }

  /**
   * 
   * @param jNode
   * @param sFieldPath
   * @param sFieldName
   * @param TValue
   *          增加内容
   *          <p>
   *          传入是数组时必须是对象类型，基本类型会被转当作单个基本类型数组，再嵌套一层数组
   *          </p>
   * @throws Exception
   */
  public static <T> void addValue(JsonNode jNode, String sFieldPath, String sFieldName, T... TValue) throws Exception {
    if (CommonUtil.ifIsNull(TValue))
      return;
    String sPath = "";
    // 方法参数校验
    if (CommonUtil.ifIsNotEmpty(sFieldPath) && CommonUtil.ifIsNotEmpty(sFieldName)) {
      sPath = sFieldPath + "." + sFieldName;
    } else if (CommonUtil.ifIsNotEmpty(sFieldPath)) {
      sPath = sFieldPath;
    } else if (CommonUtil.ifIsNotEmpty(sFieldPath)) {
      sPath = sFieldName;
    } else {
      // queryJsonArrayForce(jNode, "") = jNode
      // throw new Exception("[JsonUtil.setValue] 方法参数异常: 【设置的字段名为空】");
    }
    JsonNode node = queryJsonArrayForce(jNode, sPath);
    if (!node.isArray()) {
      throw new Exception("[JsonUtil.setValue] 方法执行异常: 【不能将现有非数组格式节点转成数组格式】");
    }
    ArrayNode aNode = (ArrayNode) node;

    for (T value : TValue) {
      if (value instanceof BigDecimal) {
        aNode.add((BigDecimal) value);
      } else if (value instanceof Boolean) {
        aNode.add((Boolean) value);
      } else if (value instanceof byte[]) {
        aNode.add((byte[]) value);
      } else if (value instanceof Double) {
        aNode.add((Double) value);
      } else if (value instanceof Float) {
        aNode.add((Float) value);
      } else if (value instanceof Integer) {
        aNode.add((Integer) value);
      } else if (value instanceof Long) {
        aNode.add((Long) value);
      } else if (value instanceof Short) {
        aNode.add((Short) value);
      } else if (value instanceof String) {
        aNode.add((String) value);
      } else if (value instanceof JsonNode) {
        aNode.add((JsonNode) value);
      } else if (value.getClass().isArray()) {
        aNode.add((JsonNode) value);
      } else {
        aNode.add(value.toString());
      }
    }
  }

  /**
   * 返回json的key列表字符串(加上前缀字符)，逗号分隔
   * 
   * @param node
   * @param prefix   前缀字符
   * @return
   */
  public static String getJsonKeyList(JsonNode node, String prefix) throws Exception {
    return getJsonKeyList(node, "", prefix);
  }

  /**
   * 返回json对象对应路径的key列表字符串(加上前缀字符)，逗号分隔
   * 
   * @param node
   * @param path     key路径
   * @param prefix   前缀字符
   * @return
   * @throws Exception
   */
  public static String getJsonKeyList(JsonNode node, String path, String prefix) throws Exception{
    String sKeyList = "";
    if (CommonUtil.ifIsNotEmpty(node)) {
      JsonNode temp = JsonUtil.queryJson(node, path);
      if (CommonUtil.ifIsNotEmpty(temp)) {
        Iterator<String> itFieldNames = temp.fieldNames();
        if (CommonUtil.ifIsEmpty(prefix)) {
          prefix = "";
        }
        while (itFieldNames.hasNext()) {
          if ("".equals(sKeyList)) {
            sKeyList = prefix + itFieldNames.next();
          } else {
            sKeyList += "," + prefix + itFieldNames.next();
          }
        }
      }
    }
    return sKeyList;
  }
}
