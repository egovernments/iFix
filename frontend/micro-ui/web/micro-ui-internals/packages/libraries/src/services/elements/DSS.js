import Urls from "../atoms/urls";
import { Request } from "../atoms/Utils/Request";

export const DSSService = {
  getDashboardConfig: (moduleCode) =>
    Request({
      url: Urls.dss.dashboardConfig + `/${moduleCode}`,
      useCache: false,
      userService: false,
      method: "GET",
      authHeader: true,
    }),
  getCharts: (data) =>
    Request({
      url: Urls.dss.getCharts,
      useCache: false,
      userService: false,
      method: "POST",
      auth: true,
      data,
    }),
  getDepartments: (data) =>
    Request({
      url: (window?.globalConfigs.getConfig('HIERARCHY_API_BASE_URL') || "").toString() + Urls.dss.search_ifix_dept_master_data,
      useCache: false,
      userService: false,
      method: "POST",
      auth: true,
      data,
    }),
  getHierarchyMetaData: (data) =>
    Request({
      url: (window?.globalConfigs.getConfig('HIERARCHY_API_BASE_URL') || "").toString() + Urls.dss.search_ifix_hierarchy_level,
      useCache: false,
      userService: false,
      method: "POST",
      auth: true,
      data
    }),
  getHierarchyData: (data) =>
    Request({
      url: (window?.globalConfigs.getConfig('HIERARCHY_API_BASE_URL') || "").toString() + Urls.dss.search_ifix_dept_entity,
      useCache: false,
      userService: false,
      method: "POST",
      auth: true,
      data,
    }),
};
