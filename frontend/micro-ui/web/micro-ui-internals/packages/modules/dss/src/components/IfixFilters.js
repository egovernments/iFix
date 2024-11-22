import { CloseSvg, FilterIcon, MultiSelectDropdown, RefreshIcon, Dropdown } from "@egovernments/digit-ui-react-components";
import React, { useContext, useEffect, useMemo, useState } from "react";
import DateRange from "./DateRange";
import FilterContext from "./FilterContext";
import Switch from "./Switch";

const IfixFilters = ({
  t,
  services,
  isOpen,
  closeFilters,
  departments,
  hierarchyLevels,
  hierarchyList,
  showDateRange = true,
  showDenomination = true,
  showModuleFilter = true,
  changeDepartment,
}) => {
  const { value, setValue } = useContext(FilterContext);
  const [selectedDept, setSelectedDept] = useState({})
  const [hierarchyLevelMapList, setHierarchyLevelMapList] = useState([]);
  const [hierarchyListMap, setHierarchyListMap] = useState({});
  const [selected, setSelected] = useState({});
  const [isFilterLoaded, setIsFilterLoaded] = useState(false);

  const customStyle = {
    filterInput: {
      marginLeft: '0%',
      marginRight: '2%'
    },
    marginBottom24: {
      marginBottom: '24px'
    },
    filterInputLast: {
      marginLeft: '0%',
      flexBasis: "16%",
      marginBottom: '24px'
    },
  };

  useEffect(() => {
    if (departments && departments.length) {
      if (value?.filters?.['Department']) {
        let idx = departments.findIndex(p => p.code==value?.filters?.['Department']);
        setSelectedDept(departments[idx]);
      } else 
        setSelectedDept(departments[0]);
    }
    if (departments && departments.length && hierarchyLevels && hierarchyLevels.length && hierarchyList && hierarchyList.length) {
      let hierarchyLevelMap = {};

      let hierarchyListMapById = {}
      hierarchyLevels.forEach((hLevel) => {
        if (hLevel.level) {
          hierarchyLevelMap[hLevel.level] = {
            ...hLevel,
            hierarchies: [],
            filterIds: [],
            selectedCodes: []
          }
        }
      });
      hierarchyList.forEach((data, idx) => {
        // create hierarchy list's map by id
        hierarchyListMapById[data?.id] = data;
        if (data.hierarchyLevel && hierarchyLevelMap[data.hierarchyLevel]) {
          hierarchyLevelMap[data.hierarchyLevel]['hierarchies'].push(data)
        }
      })
      hierarchyLevelMap = Object.values(hierarchyLevelMap);
      hierarchyLevelMap = hierarchyLevelMap.sort((x, y) => x?.hierarchyLevel?.localeCompare(y?.hierarchyLevel))
      setHierarchyLevelMapList(hierarchyLevelMap);
      setHierarchyListMap(hierarchyListMapById);
      setIsFilterLoaded(true)
    }
  }, [departments, hierarchyLevels, hierarchyList])

  const loadPreAppliedFilters = () => {
    let selectedFilters = {};
    let childHierarchyIdList = {};
    let hierarchyLevelMapUpdated = hierarchyLevelMapList.map((hierarchyMap, idx) => {
      selectedFilters[hierarchyMap.label] = [];
      let filterCodes = value?.filters?.[hierarchyMap.label] || [];
      if (filterCodes?.length) selectedFilters[hierarchyMap.label] = _.filter(hierarchyMap['hierarchies'], (hierarchyVal) => {return filterCodes.indexOf(hierarchyVal.code) != -1})

      // set list of chields based on selected filters
      hierarchyMap['selectedCodes'] = filterCodes;
      let nextLevel = hierarchyMap.level + 1;
      childHierarchyIdList[nextLevel] = []
      if (selectedFilters[hierarchyMap.label]?.length) {
        selectedFilters[hierarchyMap.label].forEach((h) => {
          if (h.children) childHierarchyIdList[nextLevel] = childHierarchyIdList[nextLevel].concat(h.children);
        })
      } else if (childHierarchyIdList[hierarchyMap.level]?.length) {
        childHierarchyIdList[hierarchyMap.level].forEach(id => {
          // Prepare child list for next level
          if (hierarchyListMap[id] && hierarchyListMap[id]?.children) childHierarchyIdList[nextLevel] = childHierarchyIdList[nextLevel].concat(hierarchyListMap[id].children);
        });
      }
      hierarchyMap['filterIds'] = childHierarchyIdList[hierarchyMap.level];
      return hierarchyMap;
    })
    setHierarchyLevelMapList(hierarchyLevelMapUpdated);
    setSelected(selectedFilters);
  }

  useEffect(() => {
    if (isFilterLoaded) {
      // set pre applied filters
      loadPreAppliedFilters()
    }
  }, [value?.filters, isFilterLoaded]);


  const [selectService, setSelectedService] = useState(() =>
    services?.filter((module) => value?.moduleLevel === module?.code)
  )

  useEffect(() => {
    setSelectedService(services?.filter((module) => value?.moduleLevel === module?.code));
  }, [value?.moduleLevel]);

  const handleFilterChange = (data) => {
    setValue({ ...value, ...data });
  };

  const selectServicesFilters = (e, data) => {
    setValue({ ...value, moduleLevel: e?.code });
  };

  const selectDepartmentFilters = (e, data) => {
    setHierarchyLevelMapList([])
    setIsFilterLoaded(false)
    setSelectedDept(e);
    changeDepartment(e);
    setValue({ ...value, filters: {'Department':  e?.code}});
  };

  const selectHierarchyFilter = (hierarchyLevel) => {
    return (e, data) => {
      if ((hierarchyLevel?.selectedCodes?.length && !e.length) || e.length) {
        changeHierarchyAndFilterChild(hierarchyLevel, e, data)
      }
    };
  }  

  const changeHierarchyAndFilterChild = (hierarchyLevel, e, data) => {
    // const hierarchyLevel = hierarchyLevels.filter((hl) => {return hl.level === e?.[0]?.[1].hierarchyLevel;})[0];
    if (hierarchyLevel) {
      let key = hierarchyLevel['label'];
      let hierarchyFilter = {};
      let selectedHierarchies = [] 
      e.forEach((appHierarchy) => { selectedHierarchies.push(appHierarchy[1]); })
      // check is any change on filter
      const isChanged = isHierarchyFilterChanged(selectedHierarchies, selected[key])
      if (!isChanged) return;
      // reset all chields
      let hierarchyLevelMapUpdated = hierarchyLevelMapList.map((hierarchy, idx) => {
        if (hierarchy.level == hierarchyLevel.level) {
          hierarchy['selectedCodes'] = selectedHierarchies.map((h) => {
            return h.code;
          })
        } else if (hierarchy.level > hierarchyLevel.level) {
          hierarchy['selectedCodes'] = [];
        }
        return hierarchy;
      })
      _.forEach(hierarchyLevelMapUpdated, (hierarchyMap) => {
        if (hierarchyMap?.selectedCodes?.length) {
          hierarchyFilter[hierarchyMap.label] = hierarchyMap.selectedCodes;
        }
      })
      // If change in filter update the filters
      setValue({ ...value, filters: { ...hierarchyFilter, 'Department':  selectedDept?.code } });
    }
  };

  const isHierarchyFilterChanged = (selectedList, prevSelectedList) => {
    if (selectedList && prevSelectedList) {
      let selectedIds = selectedList.map(a => a.id);
      let prevSelectedIds = prevSelectedList.map(a => a.id);
      if (selectedIds.sort().join(',') === prevSelectedIds.sort().join(',')) {
        return false;
      } else {
        return true;
      }
    }
    return true;
  }

  const getFilteredHierarchy = (hierarchyLevel) => {
    if (hierarchyLevel?.["filterIds"]?.length) {
      return hierarchyLevel["hierarchies"].filter((hierarchy) => hierarchyLevel["filterIds"].indexOf(hierarchy.id) != -1)
    }
    return hierarchyLevel["hierarchies"];
  }

  const handleClear = () => {
    setValue({
      denomination: "Unit",
      range: Digit.Utils.dss.getInitialRange(),
    });
  };
  return (
    <div className={`filters-wrapper ${isOpen ? "filters-modal" : ""}`} style={{
      justifyContent: window.location.href.includes("dss/dashboard/finance") && !isOpen ? "space-between" : "unset",
      paddingRight: window.location.href.includes("dss/dashboard/finance") && !isOpen ? "24px" : "0px",
      paddingBottom: window.location.href.includes("dss/dashboard/finance") && !isOpen ? "20px" : "unset",
      flexWrap: "wrap"
    }}>
      <span className="filter-close" onClick={() => closeFilters()}>
        <CloseSvg />
      </span>
      {isOpen && (
        <div className="filter-header">
          <FilterIcon />
          <p>{t(`DSS_FILTERS`)}</p>
          <span onClick={handleClear}>
            <RefreshIcon />
          </span>
        </div>
      )}
      {showDateRange && (
        <div className="filters-input" style={customStyle.filterInput}>
          <DateRange onFilterChange={handleFilterChange} values={value?.range} t={t} />
        </div>
      )}
      <div className={"filters-input " + (departments && departments.length>1 ? "":"display-none")} style={customStyle.filterInput}>
        <div className="mbsm">{t("ES_DSS_IFIX_DEPARTMENTS")}</div>
        <Dropdown
          option={departments}
          optionKey="name"
          select={selectDepartmentFilters}
          selected={selectedDept}
          placeholder={t("ES_DSS_ALL_SERVICES_SELECTED")}
        />
      </div>

      {hierarchyLevelMapList && hierarchyLevelMapList.map((hierarchyLevel, idx) => {
            return <div className="filters-input" style={idx>3 ? {...customStyle.filterInput, ...customStyle.marginBottom24}:customStyle.filterInput} key={hierarchyLevel?.level}>
              <div className="mbsm">{t("ES_DSS_IFIX_"+hierarchyLevel?.label)}</div>
              <MultiSelectDropdown
                options={getFilteredHierarchy(hierarchyLevel)}
                optionsKey="name"
                onSelect={selectHierarchyFilter(hierarchyLevel)}
                selected={selected[hierarchyLevel.label]}
                defaultLabel={t("ES_DSS_IFIX_ALL_"+ hierarchyLevel?.label)}
                defaultUnit={t("ES_DSS_IFIX_"+hierarchyLevel?.label)}
              />
            </div>
        })
      }

      {showModuleFilter && (
        <div className="filters-input" style={customStyle.filterInput}>
          <div className="mbsm">{t("ES_DSS_SERVICES")}</div>
          <Dropdown
            option={services}
            optionKey="name"
            select={selectServicesFilters}
            selected={selectService}
            placeholder={t("ES_DSS_ALL_SERVICES_SELECTED")}
          />
        </div>
      )}
      {showDenomination && (
        <div className="filters-input" style={customStyle.filterInputLast}>
          <Switch onSelect={handleFilterChange} t={t} />
        </div>
      )}
    </div>
  );
};

export default IfixFilters;