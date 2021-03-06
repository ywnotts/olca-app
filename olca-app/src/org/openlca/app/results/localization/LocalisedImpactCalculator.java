package org.openlca.app.results.localization;

import java.util.ArrayList;
import java.util.List;

import org.openlca.app.results.localization.LocalisedImpactResult.Entry;
import org.openlca.core.database.EntityCache;
import org.openlca.core.model.Location;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.results.ContributionItem;
import org.openlca.core.results.ContributionResultProvider;
import org.openlca.core.results.ContributionSet;
import org.openlca.core.results.LocationContribution;

/**
 * Calculates a localized impact assessment result.
 */
public class LocalisedImpactCalculator {

	private ContributionResultProvider<?> result;
	private LocalisedImpactMethod method;
	private List<ImpactCategoryDescriptor> impactCategories;

	public LocalisedImpactCalculator(ContributionResultProvider<?> result,
			LocalisedImpactMethod method) {
		this.result = result;
		this.method = method;
		impactCategories = new ArrayList<>();
		for (LocalisedImpactCategory locCat : method.getImpactCategories())
			impactCategories.add(locCat.getImpactCategory());
	}

	public LocalisedImpactResult calculate(EntityCache cache) {
		LocalisedImpactResult localisedResult = new LocalisedImpactResult(
				method.getImpactMethod());
		LocationContribution contributions = new LocationContribution(result);
		for (FlowDescriptor flowDescriptor : result.getFlowDescriptors()) {
			ContributionSet<Location> set = contributions
					.calculate(flowDescriptor);
			for (ContributionItem<Location> contribution : set
					.getContributions()) {
				Location location = contribution.getItem();
				double amount = contribution.getAmount();
				for (ImpactCategoryDescriptor impact : impactCategories) {
					double[] factors = findFactors(impact, flowDescriptor,
							location);
					Entry entry = new Entry();
					entry.setImpactCategory(impact);
					entry.setLocation(location);
					entry.setLocalResult(factors[0] * amount);
					entry.setResult(factors[1] * amount);
					localisedResult.addUp(entry);
				}
			}
		}
		return localisedResult;
	}

	private double[] findFactors(ImpactCategoryDescriptor impact,
			FlowDescriptor flow, Location location) {
		for (LocalisedImpactCategory localImpact : method.getImpactCategories()) {
			if (localImpact.getImpactCategory().equals(impact)) {
				double localisedVal = localImpact.getFactor(flow, location);
				double defaultVal = localImpact.getFactor(flow,
						method.getDefaultLocation());
				if (localisedVal == 0)
					localisedVal = defaultVal;
				return new double[] { localisedVal, defaultVal };
			}
		}
		return new double[] { 0, 0 };
	}

}
