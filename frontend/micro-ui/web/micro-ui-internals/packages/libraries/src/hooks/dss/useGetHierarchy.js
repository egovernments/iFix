import { useQuery } from "react-query";
import { DSSService } from "../../services/elements/DSS";

const useGetHierarchy = (tenantId, departmentId, config={}) => {
  let data = {
        "requestHeader": {
         "version": "2.0.0"
       },
       "criteria": {
         tenantId,
         departmentId
       }
     }
  return useQuery(["DSS_HIERARCHY_LIST", tenantId, departmentId], () => DSSService.getHierarchyData(data), config);
};

export default useGetHierarchy;