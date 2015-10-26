package com.teamhub.plugins.netjets.managers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.teamhub.controllers.utils.PaginatedList;
import com.teamhub.infrastructure.hibernate.SessionFactoryWrapper;
import com.teamhub.infrastructure.spring.RequestInfo;
import com.teamhub.infrastructure.utils.reflection.AutowireStatic;
import com.teamhub.managers.generic.DirectQueryManager;
import com.teamhub.managers.node.NodeManager;
import com.teamhub.managers.node.NodeQueryPlanner;
import com.teamhub.managers.site.SiteManager;
import com.teamhub.models.node.Node;
import com.teamhub.models.node.Question;
import com.teamhub.models.site.Container;
import com.teamhub.models.site.Network;
import com.teamhub.models.site.Site;

@Service
@AutowireStatic
public class NetjetsApiManager {
	@Autowired
	DirectQueryManager queryManager;

	@Autowired
	NodeManager nodeManager;

	@Autowired
	SiteManager siteManager;

	@Autowired
	RequestInfo requestInfo;

	@Autowired
	SessionFactoryWrapper sessionFactoryWrapper;

	@Transactional
	public PaginatedList getQuestionsBySpace(final Container container,
			final String space, final String sort, final Integer page,
			final Integer pageSize) {
		final PaginatedList list = (PaginatedList) this.sessionFactoryWrapper
				.runOnNetwork(new Callable() {
					@Override
					public Object call() throws Exception {
						final NodeQueryPlanner<Question> p = NetjetsApiManager.this.nodeManager
								.getQueryPlanner(Question.class)
								.pageSize(pageSize).pageNumber(page)
								.namedSort(sort);

						p.inContainer(NetjetsApiManager.this.siteManager
								.getSpaceByName(space, (Site) container));

						final List<Question> questions = p.execute();

						final Map<String, Object> additionalParams = new HashMap<String, Object>();
						additionalParams.put("customPageSize", true);

						return new PaginatedList(questions, sort, page,
								pageSize, p.getCount(), Node.Sorts.values(),
								additionalParams);
					}
				}, (Network) container.getParent());

		return list;
	}
}
