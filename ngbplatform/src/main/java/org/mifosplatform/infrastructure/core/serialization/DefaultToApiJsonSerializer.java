/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.serialization;

import java.util.Collection;
import java.util.Set;

import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.infrastructure.jobs.data.JobDetailHistoryData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

/**
 * An abstract helper implementation of {@link ToApiJsonSerializer} for
 * resources to serialize their Java data objects into JSON.
 */
@Component
public final class DefaultToApiJsonSerializer<T> implements ToApiJsonSerializer<T> {

	private final ExcludeNothingWithPrettyPrintingOffJsonSerializerGoogleGson excludeNothingWithPrettyPrintingOff;
	private final ExcludeNothingWithPrettyPrintingOffWithTimeJsonSerializerGoogleGson excludeNothingWithPrettyPrintingTimeOff;
	private final ExcludeNothingWithPrettyPrintingOnJsonSerializerGoogleGson excludeNothingWithPrettyPrintingOn;
	private final ExcludeNothingWithPrettyPrintingOnWithTimeJsonSerializerGoogleGson excludeNothingWithPrettyPrintingTimeOn;

	private final CommandProcessingResultJsonSerializer commandProcessingResultSerializer;
	private final GoogleGsonSerializerHelper helper;

	@Autowired
	public DefaultToApiJsonSerializer(
			final ExcludeNothingWithPrettyPrintingOffJsonSerializerGoogleGson excludeNothingWithPrettyPrintingOff,
			final ExcludeNothingWithPrettyPrintingOnJsonSerializerGoogleGson excludeNothingWithPrettyPrintingOn,
			final CommandProcessingResultJsonSerializer commandProcessingResultSerializer,
			final ExcludeNothingWithPrettyPrintingOffWithTimeJsonSerializerGoogleGson excludeNothingWithPrettyPrintingTimeOff,
			final ExcludeNothingWithPrettyPrintingOnWithTimeJsonSerializerGoogleGson excludeNothingWithPrettyPrintingTimeOn,
			final GoogleGsonSerializerHelper helper) {
		this.excludeNothingWithPrettyPrintingOff = excludeNothingWithPrettyPrintingOff;
		this.excludeNothingWithPrettyPrintingOn = excludeNothingWithPrettyPrintingOn;
		this.commandProcessingResultSerializer = commandProcessingResultSerializer;
		this.excludeNothingWithPrettyPrintingTimeOff = excludeNothingWithPrettyPrintingTimeOff;
		this.excludeNothingWithPrettyPrintingTimeOn = excludeNothingWithPrettyPrintingTimeOn;
		this.helper = helper;

	}

	@Override
	public String serializeResult(final Object object) {
		return this.commandProcessingResultSerializer.serialize(object);
	}

	@Override
	public String serialize(final Object object) {
		return this.excludeNothingWithPrettyPrintingOff.serialize(object);
	}

	@Override
	public String serializeDateTime(final Object object) {
		return this.excludeNothingWithPrettyPrintingTimeOff.serialize(object);
	}

	@Override
	public String serializePretty(boolean prettyOn, final Object object) {
		String json = "";

		if (prettyOn) {
			json = this.excludeNothingWithPrettyPrintingOn.serialize(object);
		} else {
			json = serialize(object);
		}
		return json;
	}

	@Override
	public String serializeWithTime(final Object object) {
		return this.excludeNothingWithPrettyPrintingTimeOff.serialize(object);
	}

	@Override
	public String serializeWithTimePretty(boolean prettyOn, final Object object) {
		String json = "";

		if (prettyOn) {
			json = this.excludeNothingWithPrettyPrintingTimeOn.serializeWithTime(object);
		} else {
			json = serialize(object);
		}
		return json;
	}

	@Override
	public String serialize(final ApiRequestJsonSerializationSettings settings, final Collection<T> collection,
			final Set<String> supportedResponseParameters) {
		final Gson delegatedSerializer = findAppropriateSerializer(settings, supportedResponseParameters);
		return serializeWithSettings(delegatedSerializer, settings, collection.toArray());
	}

	@Override
	public String serialize(final ApiRequestJsonSerializationSettings settings, final T singleObject,
			final Set<String> supportedResponseParameters) {
		final Gson delegatedSerializer = findAppropriateSerializer(settings, supportedResponseParameters);
		return serializeWithSettings(delegatedSerializer, settings, singleObject);
	}

	private String serializeWithSettings(final Gson gson, final ApiRequestJsonSerializationSettings settings,
			final Object[] dataObject) {
		String json = null;
		if (gson != null) {
			json = helper.serializedJsonFrom(gson, dataObject);
		} else {
			if (settings.isPrettyPrint()) {
				json = this.excludeNothingWithPrettyPrintingOn.serialize(dataObject);
			} else {
				json = serialize(dataObject);
			}
		}
		return json;
	}

	private String serializeWithSettings(final Gson gson, final ApiRequestJsonSerializationSettings settings,
			final Object dataObject) {
		String json = null;
		if (gson != null) {
			json = helper.serializedJsonFrom(gson, dataObject);
		} else {
			if (settings.isPrettyPrint()) {
				json = this.excludeNothingWithPrettyPrintingOn.serialize(dataObject);
			} else {
				json = serialize(dataObject);
			}
		}
		return json;
	}

	@Override
	public String serializetime(final ApiRequestJsonSerializationSettings settings, final T singleObject,
			final Set<String> supportedResponseParameters) {
		final Gson delegatedSerializer = findAppropriateSerializer(settings, supportedResponseParameters);
		return serializetimeWithSettings(delegatedSerializer, settings, singleObject);
	}

	private String serializetimeWithSettings(final Gson gson, final ApiRequestJsonSerializationSettings settings,
			final Object dataObject) {
		String json = null;
		if (gson != null) {
			json = helper.serializedJsonFrom(gson, dataObject);
		} else {
			if (settings.isPrettyPrint()) {
				json = this.excludeNothingWithPrettyPrintingTimeOn.serializeWithTime(dataObject);
			} else {
				json = serializeDateTime(dataObject);
			}
		}
		return json;
	}

	private Gson findAppropriateSerializer(final ApiRequestJsonSerializationSettings settings,
			final Set<String> supportedResponseParameters) {
		Gson gson = null;
		if (settings.isPartialResponseRequired()) {
			gson = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(supportedResponseParameters,
					settings.isPrettyPrint(), settings.getParametersForPartialResponse());
		}
		return gson;
	}

	@Override
	public String serialize(ApiRequestJsonSerializationSettings settings,
			Page<JobDetailHistoryData> jobhistoryDetailData, Set<String> jobHistoryResponseDataParameters) {
		final Gson delegatedSerializer = findAppropriateSerializer(settings, jobHistoryResponseDataParameters);
		return serializeWithSettings(delegatedSerializer, settings, jobhistoryDetailData);
	}
}