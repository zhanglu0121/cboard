package org.cboard.services;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.google.common.base.Functions;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.cboard.dao.DatasetDao;
import org.cboard.dao.DatasourceDao;
import org.cboard.dao.UserBranchDao;
import org.cboard.dataprovider.DataProvider;
import org.cboard.dataprovider.DataProviderManager;
import org.cboard.dataprovider.config.AggConfig;
import org.cboard.dataprovider.config.DimensionConfig;
import org.cboard.dataprovider.result.AggregateResult;
import org.cboard.dto.DataProviderResult;
import org.cboard.exception.CBoardException;
import org.cboard.pojo.DashboardDataset;
import org.cboard.pojo.DashboardDatasource;
import org.cboard.pojo.DashboardUser;
import org.cboard.util.IPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.function.Consumer;

/**
 * Created by yfyuan on 2016/8/15.
 */
@Repository
public class DataProviderService {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DatasourceDao datasourceDao;
    @Autowired
    private DatasetDao datasetDao;
    @Autowired
    private UserBranchDao userBranchDao;
    @Autowired
    private HttpSession session;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private HttpServletRequest request;

    private DataProvider getDataProvider(Long datasourceId, Map<String, String> query, Dataset dataset) throws Exception {
        if (dataset != null) {
            datasourceId = dataset.getDatasourceId();
            query = dataset.getQuery();
        }
        DashboardDatasource datasource = datasourceDao.getDatasource(datasourceId);
        JSONObject datasourceConfig = JSONObject.parseObject(datasource.getConfig());
        Map<String, String> dataSource = Maps.transformValues(datasourceConfig, Functions.toStringFunction());
        DataProvider dataProvider = DataProviderManager.getDataProvider(datasource.getType(), dataSource, query);
        if (dataset != null && dataset.getInterval() != null && dataset.getInterval() > 0) {
            dataProvider.setInterval(dataset.getInterval());
        }
        return dataProvider;
    }

    public Map<String, String> getDataSource(Long datasourceId) {
        DashboardDatasource datasource = datasourceDao.getDatasource(datasourceId);
        JSONObject datasourceConfig = JSONObject.parseObject(datasource.getConfig());
        return Maps.transformValues(datasourceConfig, Functions.toStringFunction());
    }

    public AggregateResult queryAggData(Long datasourceId, Map<String, String> query, Long datasetId, AggConfig config, boolean reload) {
        try {
            Dataset dataset = getDataset(datasetId);
            attachCustom(dataset, config);
            DataProvider dataProvider = getDataProvider(datasourceId, query, dataset);
            return dataProvider.getAggData(config, reload);
        } catch (Exception e) {
            LOG.error("", e);
            throw new CBoardException(e.getMessage());
        }
    }

    public DataProviderResult getColumns(Long datasourceId, Map<String, String> query, Long datasetId, boolean reload) {
        DataProviderResult dps = new DataProviderResult();
        try {
            Dataset dataset = getDataset(datasetId);
            DataProvider dataProvider = getDataProvider(datasourceId, query, dataset);
            String[] result = dataProvider.invokeGetColumn(reload);
            dps.setColumns(result);
            dps.setMsg("1");
        } catch (Exception e) {
            LOG.error("", e);
            dps.setMsg(e.getMessage());
        }
        return dps;
    }

    public String[] getDimensionValues(Long datasourceId, Map<String, String> query, Long datasetId, String columnName, AggConfig config, boolean reload) {
        try {
            Dataset dataset = getDataset(datasetId);
            attachCustom(dataset, config);
            DataProvider dataProvider = getDataProvider(datasourceId, query, dataset);
            String[] result = dataProvider.getDimVals(columnName, config, reload);
            return result;
        } catch (Exception e) {
            LOG.error("", e);
        }
        return null;
    }

    public String viewAggDataQuery(Long datasourceId, Map<String, String> query, Long datasetId, AggConfig config) {
        try {
            Dataset dataset = getDataset(datasetId);
            attachCustom(dataset, config);
            DataProvider dataProvider = getDataProvider(datasourceId, query, dataset);
            return dataProvider.getViewAggDataQuery(config);
        } catch (Exception e) {
            LOG.error("", e);
            throw new CBoardException(e.getMessage());
        }
    }

    public ServiceStatus test(JSONObject dataSource, Map<String, String> query) {
        try {
            DataProvider dataProvider = DataProviderManager.getDataProvider(
                    dataSource.getString("type"),
                    Maps.transformValues(dataSource.getJSONObject("config"), Functions.toStringFunction()),
                    query, true);
            dataProvider.test();
            return new ServiceStatus(ServiceStatus.Status.Success, null);
        } catch (Exception e) {
            LOG.error("", e);
            return new ServiceStatus(ServiceStatus.Status.Fail, e.getMessage());
        }
    }

    public boolean isDataSourceAggInstance(Long datasourceId, Map<String, String> query, Long datasetId) {
        try {
            Dataset dataset = getDataset(datasetId);
            DataProvider dataProvider = getDataProvider(datasourceId, query, dataset);
            return dataProvider.isDataSourceAggInstance();
        } catch (Exception e) {
            LOG.error("", e);
            throw new CBoardException(e.getMessage());
        }
    }

    private void attachCustom(Dataset dataset, AggConfig aggConfig) {
        if (dataset == null || aggConfig == null) {
            return;
        }
        Consumer<DimensionConfig> predicate = (config) -> {
            if (StringUtils.isNotEmpty(config.getId())) {
                String custom = (String) JSONPath.eval(dataset.getSchema(), "$.dimension[id='" + config.getId() + "'][0].custom");
                if (custom == null) {
                    custom = (String) JSONPath.eval(dataset.getSchema(), "$.dimension[type='level'].columns[id='" + config.getId() + "'][0].custom");
                }
                config.setCustom(custom);
            }
        };
        aggConfig.getRows().forEach(predicate);
        aggConfig.getColumns().forEach(predicate);
    }


    public String getBranchSql(String sql) {
        DashboardUser user = getUserBranch();
        if (user != null) {
            while (sql.contains("orgAuth")) {
                sql = sql.replace("orgAuth", "(" + user.getBranchAuthority() + ")");
            }
            while (sql.contains("siteAuth")) {
                sql = sql.replace("siteAuth", "(" + user.getSiteAuthority() + ")");
            }
            while (sql.contains("checkUpAuth")) {
                sql = sql.replace("checkUpAuth", "(" + user.getCheckUpList() + ")");
            }

        }
        return sql;
    }

    public DashboardUser getUserBranch() {
//        String loginName = (String) session.getAttribute("currentLoginName");
        String ipAddr = IPUtil.getIpAddr(request);
        String loginName = (String) redisTemplate.boundHashOps("CboardUser").get(ipAddr);
        List<String> branchList;
        List<String> siteList;
        List<String> checkUpList;
        if ("admin".equals(loginName)) {
            branchList = userBranchDao.getAllBranch();
            siteList = userBranchDao.getAllSite();
            checkUpList = userBranchDao.getCheckUpAuth(loginName);
        } else {
            branchList = userBranchDao.getBranchAuth(loginName);
            siteList = userBranchDao.getSiteAuth(loginName);
            checkUpList = userBranchDao.getCheckUpAuth(loginName);
        }
        DashboardUser user = new DashboardUser();
        if (branchList == null || branchList.size() < 1) {
            user.setBranchAuthority("''");
        } else {
            user.setBranchAuthority(listToString(branchList));
        }
        if (siteList == null || siteList.size() < 1) {
            user.setSiteAuthority("''");
        } else {
            user.setSiteAuthority(listToString(siteList));
        }
        if (checkUpList == null || checkUpList.size() < 1) {
            user.setCheckUpList("''");
        } else {
            user.setCheckUpList(listToString(checkUpList));
        }
        user.setLoginName(loginName);
        return user;
    }

    //list转字符串
    public String listToString(List<String> list) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < list.size(); i++) {
            String s = list.get(i);
            if (i == 0) {
                buffer.append("'" + s + "'");
            } else {
                buffer.append(",'" + s + "'");
            }
        }
        String str = buffer.toString();
        return str;
    }

    protected Dataset getDataset(Long datasetId) {
        if (datasetId == null) {
            return null;
        }
        Dataset dataset = new Dataset(datasetDao.getDataset(datasetId));
        if (dataset != null && dataset.getQuery().size() > 0) {
            Map<String, String> map = new HashMap<>();
            Map<String, String> query = dataset.getQuery();
            String sql = query.get("sql");
            if (sql.contains("orgAuth") || sql.contains("siteAuth") || sql.contains("branchAuth")) {
                sql = getBranchSql(sql);
                map.put("sql", sql);
                dataset.setQuery(map);
            }
        }
        return dataset;
    }

    protected class Dataset {
        private Long datasourceId;
        private Map<String, String> query;
        private Long interval;
        private JSONObject schema;

        public Dataset(DashboardDataset dataset) {
            JSONObject data = JSONObject.parseObject(dataset.getData());
            this.query = Maps.transformValues(data.getJSONObject("query"), Functions.toStringFunction());
            this.datasourceId = data.getLong("datasource");
            this.interval = data.getLong("interval");
            this.schema = data.getJSONObject("schema");
        }

        public JSONObject getSchema() {
            return schema;
        }

        public void setSchema(JSONObject schema) {
            this.schema = schema;
        }

        public Long getDatasourceId() {
            return datasourceId;
        }

        public void setDatasourceId(Long datasourceId) {
            this.datasourceId = datasourceId;
        }

        public Map<String, String> getQuery() {
            return query;
        }

        public void setQuery(Map<String, String> query) {
            this.query = query;
        }

        public Long getInterval() {
            return interval;
        }

        public void setInterval(Long interval) {
            this.interval = interval;
        }
    }
}
