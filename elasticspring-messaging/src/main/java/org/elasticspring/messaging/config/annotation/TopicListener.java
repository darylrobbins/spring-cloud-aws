/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.elasticspring.messaging.config.annotation;

import org.elasticspring.core.support.documentation.RuntimeUse;

/**
 * @author Agim Emruli
 * @since 1.0
 */
public @interface TopicListener {

	String topicName();

	NotificationProtocol protocol();

	String endpoint();

	/**
	 * @author Agim Emruli
	 * @since 1.0
	 */
	enum NotificationProtocol {

		@RuntimeUse
		HTTP("http"),
		@RuntimeUse
		HTTPS("https");

		private final String canonicalName;

		NotificationProtocol(String canonicalName) {
			this.canonicalName = canonicalName;
		}

		public String getCanonicalName() {
			return this.canonicalName;
		}
	}
}