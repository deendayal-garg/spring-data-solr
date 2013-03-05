/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.solr.repository.query;

import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.solr.core.QueryParser;
import org.springframework.data.solr.core.SolrOperations;
import org.springframework.data.solr.core.geo.Distance;
import org.springframework.data.solr.core.geo.GeoLocation;
import org.springframework.data.solr.repository.ProductBean;
import org.springframework.data.solr.repository.Query;
import org.springframework.data.solr.server.SolrServerFactory;

/**
 * @author Christoph Strobl
 */
@RunWith(MockitoJUnitRunner.class)
public class StringBasedSolrQueryTests {

	@Mock
	private SolrOperations solrOperationsMock;

	@Mock
	private RepositoryMetadata metadataMock;

	@Mock
	private SolrEntityInformationCreator entityInformationCreatorMock;

	@Mock
	SolrServerFactory solrServerFactoryMock;

	private QueryParser queryParser;

	@Before
	public void setUp() {
		this.queryParser = new QueryParser();
	}

	@Test
	public void testQueryCreationSingleProperty() throws NoSuchMethodException, SecurityException {
		Method method = SampleRepository.class.getMethod("findByText", String.class);
		SolrQueryMethod queryMethod = new SolrQueryMethod(method, metadataMock, entityInformationCreatorMock);

		StringBasedSolrQuery solrQuery = new StringBasedSolrQuery(queryMethod, solrOperationsMock);

		org.springframework.data.solr.core.query.Query query = solrQuery.createQuery(new SolrParametersParameterAccessor(
				queryMethod, new Object[] { "j73x73r" }));

		Assert.assertEquals("textGeneral:j73x73r", queryParser.getQueryString(query));
	}

	@Test
	public void testQueryCreationWithNegativeProperty() throws NoSuchMethodException, SecurityException {
		Method method = SampleRepository.class.getMethod("findByPopularityAndPrice", Integer.class, Float.class);
		SolrQueryMethod queryMethod = new SolrQueryMethod(method, metadataMock, entityInformationCreatorMock);

		StringBasedSolrQuery solrQuery = new StringBasedSolrQuery(queryMethod, solrOperationsMock);

		org.springframework.data.solr.core.query.Query query = solrQuery.createQuery(new SolrParametersParameterAccessor(
				queryMethod, new Object[] { Integer.valueOf(-1), Float.valueOf(-2f) }));

		Assert.assertEquals("popularity:\\-1 AND price:\\-2.0", queryParser.getQueryString(query));
	}

	@Test
	public void testQueryCreationMultiyProperty() throws NoSuchMethodException, SecurityException {
		Method method = SampleRepository.class.getMethod("findByPopularityAndPrice", Integer.class, Float.class);
		SolrQueryMethod queryMethod = new SolrQueryMethod(method, metadataMock, entityInformationCreatorMock);

		StringBasedSolrQuery solrQuery = new StringBasedSolrQuery(queryMethod, solrOperationsMock);

		org.springframework.data.solr.core.query.Query query = solrQuery.createQuery(new SolrParametersParameterAccessor(
				queryMethod, new Object[] { Integer.valueOf(1), Float.valueOf(2f) }));

		Assert.assertEquals("popularity:1 AND price:2.0", queryParser.getQueryString(query));
	}

	@Test
	public void testQueryCreationWithNullProperty() throws NoSuchMethodException, SecurityException {
		Method method = SampleRepository.class.getMethod("findByText", String.class);
		SolrQueryMethod queryMethod = new SolrQueryMethod(method, metadataMock, entityInformationCreatorMock);

		StringBasedSolrQuery solrQuery = new StringBasedSolrQuery(queryMethod, solrOperationsMock);

		org.springframework.data.solr.core.query.Query query = solrQuery.createQuery(new SolrParametersParameterAccessor(
				queryMethod, new Object[] { null }));

		Assert.assertEquals("textGeneral:null", queryParser.getQueryString(query));
	}

	@Test
	public void testWithGeoLocationProperty() throws NoSuchMethodException, SecurityException {
		Method method = SampleRepository.class.getMethod("findByLocationNear", GeoLocation.class, Distance.class);
		SolrQueryMethod queryMethod = new SolrQueryMethod(method, metadataMock, entityInformationCreatorMock);

		StringBasedSolrQuery solrQuery = new StringBasedSolrQuery(queryMethod, solrOperationsMock);

		org.springframework.data.solr.core.query.Query query = solrQuery.createQuery(new SolrParametersParameterAccessor(
				queryMethod, new Object[] { new GeoLocation(48.303056, 14.290556), new Distance(5) }));

		Assert.assertEquals("{!geofilt pt=48.303056,14.290556 sfield=store d=5.0}", queryParser.getQueryString(query));
	}

	@Test
	public void testWithGeoLocationPropertyWhereDistanceIsInMiles() throws NoSuchMethodException, SecurityException {
		Method method = SampleRepository.class.getMethod("findByLocationNear", GeoLocation.class, Distance.class);
		SolrQueryMethod queryMethod = new SolrQueryMethod(method, metadataMock, entityInformationCreatorMock);

		StringBasedSolrQuery solrQuery = new StringBasedSolrQuery(queryMethod, solrOperationsMock);

		org.springframework.data.solr.core.query.Query query = solrQuery.createQuery(new SolrParametersParameterAccessor(
				queryMethod, new Object[] { new GeoLocation(48.303056, 14.290556), new Distance(1, Distance.Unit.MILES) }));

		Assert.assertEquals("{!geofilt pt=48.303056,14.290556 sfield=store d=1.609344}", queryParser.getQueryString(query));
	}

	@Test
	public void testWithProjectionOnSingleField() throws NoSuchMethodException, SecurityException {
		Method method = SampleRepository.class.getMethod("findByNameProjectionOnPopularity", String.class);
		SolrQueryMethod queryMethod = new SolrQueryMethod(method, metadataMock, entityInformationCreatorMock);

		StringBasedSolrQuery solrQuery = new StringBasedSolrQuery(queryMethod, solrOperationsMock);

		org.springframework.data.solr.core.query.Query query = solrQuery.createQuery(new SolrParametersParameterAccessor(
				queryMethod, new Object[] { "christoph" }));

		Assert.assertEquals("name:christoph*", queryParser.getQueryString(query));
		Assert.assertEquals(1, query.getProjectionOnFields().size());
		Assert.assertEquals("popularity", query.getProjectionOnFields().get(0).getName());
	}

	@Test
	public void testWithProjectionOnMultipleFields() throws NoSuchMethodException, SecurityException {
		Method method = SampleRepository.class.getMethod("findByNameProjectionOnPopularityAndPrice", String.class);
		SolrQueryMethod queryMethod = new SolrQueryMethod(method, metadataMock, entityInformationCreatorMock);

		StringBasedSolrQuery solrQuery = new StringBasedSolrQuery(queryMethod, solrOperationsMock);

		org.springframework.data.solr.core.query.Query query = solrQuery.createQuery(new SolrParametersParameterAccessor(
				queryMethod, new Object[] { "strobl" }));

		Assert.assertEquals("name:strobl*", queryParser.getQueryString(query));
		Assert.assertEquals(2, query.getProjectionOnFields().size());
		Assert.assertEquals("popularity", query.getProjectionOnFields().get(0).getName());
		Assert.assertEquals("price", query.getProjectionOnFields().get(1).getName());
	}

	private interface SampleRepository {

		@Query("textGeneral:?0")
		ProductBean findByText(String text);

		@Query("popularity:?0 AND price:?1")
		ProductBean findByPopularityAndPrice(Integer popularity, Float price);

		@Query("{!geofilt pt=?0 sfield=store d=?1}")
		ProductBean findByLocationNear(GeoLocation location, Distance distace);

		@Query(value = "name:?0*", fields = "popularity")
		ProductBean findByNameProjectionOnPopularity(String name);

		@Query(value = "name:?0*", fields = { "popularity", "price" })
		ProductBean findByNameProjectionOnPopularityAndPrice(String name);

	}

}