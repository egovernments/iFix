import { useQuery } from "react-query";
import { DSSService } from "../../services/elements/DSS";

const useGetDepartments = (tenantId, config = {}) => {
  let data = {
    "requestHeader": {
      "version": "2.0.0"
    },
    "criteria": {
      tenantId
    }
  }

  return useQuery(["DSS_HIERARCHY_DEPT", tenantId], () => DSSService.getDepartments(data), config);

};

export default useGetDepartments;