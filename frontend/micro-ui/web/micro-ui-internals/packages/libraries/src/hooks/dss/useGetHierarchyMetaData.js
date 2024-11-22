import { useQuery } from "react-query";
import { DSSService } from "../../services/elements/DSS";

const useGetHierarchyMetaData = (tenantId, departmentId, config={}) => {
  let data = {
    "requestHeader": {
        "version": "2.0.0"
    },
    "criteria": {
        tenantId,
        departmentId
    }
  }
  // return DSSService.getDepartments(data);
  return useQuery(["DSS_HIERARCHY_METADATA", tenantId, departmentId], () => DSSService.getHierarchyMetaData(data), config);
};

export default useGetHierarchyMetaData;