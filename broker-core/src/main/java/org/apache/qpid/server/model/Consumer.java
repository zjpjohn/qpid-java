/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.qpid.server.model;

import org.apache.qpid.server.consumer.ConsumerImpl;

@ManagedObject
public interface Consumer<X extends Consumer<X>> extends ConfiguredObject<X>, ConsumerImpl
{
    String DISTRIBUTION_MODE = "distributionMode";
    String EXCLUSIVE = "exclusive";
    String NO_LOCAL = "noLocal";
    String SELECTOR = "selector";
    String SETTLEMENT_MODE = "settlementMode";
    String PRIORITY = "priority";


    String SUSPEND_NOTIFICATION_PERIOD = "consumer.suspendNotificationPeriod";

    @ManagedContextDefault( name = SUSPEND_NOTIFICATION_PERIOD)
    long SUSPEND_NOTIFICATION_PERIOD_DEFAULT = 10000;

    @ManagedAttribute
    String getDistributionMode();

    @ManagedAttribute
    String getSettlementMode();

    @ManagedAttribute
    boolean isExclusive();

    @ManagedAttribute
    boolean isNoLocal();

    @ManagedAttribute
    String getSelector();

    @ManagedAttribute(defaultValue = "0",
                      description="Non-negative number representing the priority of the consumer versus other "
                                  + "consumers.  Priority 0 is the highest priority.")
    int getPriority();

    @ManagedStatistic(statisticType = StatisticType.CUMULATIVE, units = StatisticUnit.BYTES, label = "Outbound")
    long getBytesOut();

    @ManagedStatistic(statisticType = StatisticType.CUMULATIVE, units = StatisticUnit.MESSAGES, label = "Outbound")
    long getMessagesOut();

    @ManagedStatistic(statisticType = StatisticType.POINT_IN_TIME, units = StatisticUnit.BYTES, label = "Prefetch")
    long getUnacknowledgedBytes();

    @ManagedStatistic(statisticType = StatisticType.POINT_IN_TIME, units = StatisticUnit.MESSAGES, label = "Prefetch")
    long getUnacknowledgedMessages();

}
