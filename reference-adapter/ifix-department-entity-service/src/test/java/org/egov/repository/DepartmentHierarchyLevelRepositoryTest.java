package org.egov.repository;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.query.Query;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

@Disabled("TODO: Need to work on it")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
class DepartmentHierarchyLevelRepositoryTest {
/*
    @InjectMocks
    private DepartmentHierarchyLevelRepository_old departmentHierarchyLevelRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private DepartmentHierarchyLevelQueryBuilder queryBuilder;

    @Test
    void testSearchDeptHierarchyLevelWithDefaultSearchCriteria() {
        assertTrue(departmentHierarchyLevelRepository.
                searchDeptHierarchyLevel(new DepartmentHierarchyLevelSearchCriteria()).isEmpty());
    }

    @Test
    void testSearchDeptHierarchyLevelWithSearchCriteria() {
        assertTrue(departmentHierarchyLevelRepository.searchDeptHierarchyLevel(
                DepartmentHierarchyLevelSearchCriteria.builder()
                        .tenantId("pb")
                        .departmentId("7bdf9514-e2e5-4563-bfea-f5aaa41b2137")
                        .level(0)
                        .label("Department")
                        .build()).isEmpty());
    }

    @Test
    void testSearchDeptHierarchyLevelWithSearchCriteriaAndResult() {
        DepartmentHierarchyLevelSearchCriteria searchCriteria = DepartmentHierarchyLevelSearchCriteria.builder()
                .tenantId("pb")
                .departmentId("7bdf9514-e2e5-4563-bfea-f5aaa41b2137")
                .level(0)
                .label("Department")
                .build();
        doReturn(new Query()).when(queryBuilder).buildSearchQuery(searchCriteria);
        List<DepartmentHierarchyLevel> departmentHierarchyLevelList = new ArrayList<>();
        departmentHierarchyLevelList.add(new DepartmentHierarchyLevel());
        doReturn(departmentHierarchyLevelList).when(mongoTemplate).find(any(), any(DepartmentHierarchyLevel.class.getClass()));
        assertTrue(!departmentHierarchyLevelRepository.searchDeptHierarchyLevel(searchCriteria).isEmpty());
    }

    @Test
    void testSearchParentDeptHierarchyLevelWithEmptyResult() {
        assertTrue(departmentHierarchyLevelRepository.searchParentDeptHierarchyLevel("7bdf9514-e2e5-4563-bfea-f5aaa41b2137",
                "pb", "parent").isEmpty());
    }

    @Test
    void testSearchParentDeptHierarchyLevelWithResult() {
        doReturn(new Query()).when(queryBuilder).buildParentDeptHierarchyLevelSearchQuery("7bdf9514-e2e5-4563-bfea-f5aaa41b2137",
                "pb", "parent");
        List<DepartmentHierarchyLevel> departmentHierarchyLevelList = new ArrayList<>();
        departmentHierarchyLevelList.add(new DepartmentHierarchyLevel());
        doReturn(departmentHierarchyLevelList).when(mongoTemplate).find(any(), any(DepartmentHierarchyLevel.class.getClass()));


        assertTrue(!departmentHierarchyLevelRepository.searchParentDeptHierarchyLevel("7bdf9514-e2e5-4563-bfea-f5aaa41b2137",
                "pb", "parent").isEmpty());
    }*/
}