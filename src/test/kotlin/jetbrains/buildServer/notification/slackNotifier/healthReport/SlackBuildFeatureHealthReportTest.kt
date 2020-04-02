package jetbrains.buildServer.notification.slackNotifier.healthReport

import jetbrains.buildServer.notification.FeatureProviderNotificationRulesHolder
import jetbrains.buildServer.notification.slackNotifier.And
import jetbrains.buildServer.notification.slackNotifier.slack.AggregatedSlackApi
import jetbrains.buildServer.serverSide.impl.NotificationRulesBuildFeature
import org.testng.annotations.Test

class SlackBuildFeatureHealthReportTest : BaseSlackHealthReportTest<SlackBuildFeatureHealthReport>() {
    override fun getReport(): SlackBuildFeatureHealthReport {
        return SlackBuildFeatureHealthReport(
            myDescriptor,
            myConnectionManager,
            mySlackApiFactory,
            AggregatedSlackApi(mySlackApiFactory)
        )
    }

    @Test
    fun `test should report missing connection`() {
        `given build type is in scope`() And
                `given build feature is missing connection`()
        `when health is reported`()
        `then report should contain error about missing connection`()
    }

    @Test
    fun `test should report invalid connection`() {
        `given build type is in scope`() And
                `given build feature has invalid connection`()
        `when health is reported`()
        `then report should contain error about invalid connection`()
    }

    @Test
    fun `test should report invalid channel`() {
        `given build type is in scope`() And
                `given build feature has invalid channel`()
        `when health is reported`()
        `then report should contain error about invalid channel`()
    }

    @Test
    fun `test should report channel that bot is not added to`() {
        `given build type is in scope`() And
                `given build feature has channel that bot is not added to`()
        `when health is reported`()
        `then report should contain error about not being added to channel`()
    }

    @Test
    fun `test should not report if build feature is configured correctly`() {
        `given build type is in scope`() And
                `given build features is configured correctly`()
        `when health is reported`()
        `then report should contain no errors`()
    }

    private fun `given build feature is missing connection`() {
        addBuildFeature()
    }

    private fun `given build feature has invalid connection`() {
        addBuildFeature(mapOf(myDescriptor.connectionProperty.key to "invalid_connection"))
    }

    private fun `given build feature has invalid channel`() {
        addBuildFeature(
            mapOf(
                myDescriptor.connectionProperty.key to myConnection.id,
                myDescriptor.channelProperty.key to "#invalid_channel"
            )
        )
    }

    private fun `given build feature has channel that bot is not added to`() {
        addBuildFeature(
            mapOf(
                myDescriptor.connectionProperty.key to myConnection.id,
                myDescriptor.channelProperty.key to "#anotherChannel"
            )
        )
    }

    private fun `given build features is configured correctly`() {
        addBuildFeature(
            mapOf(
                myDescriptor.connectionProperty.key to myConnection.id,
                myDescriptor.channelProperty.key to "#test_channel"
            )
        )
    }

    private fun addBuildFeature(parameters: Map<String, String> = emptyMap()) {
        myBuildType.addBuildFeature(
            NotificationRulesBuildFeature.FEATURE_TYPE,
            mapOf(FeatureProviderNotificationRulesHolder.NOTIFIER to myDescriptor.type) + parameters
        )
    }

    private fun `then report should contain error about missing connection`() {
        assertResultContains {
            it.category == SlackBuildFeatureHealthReport.invalidBuildFeatureCategory &&
                    (it.additionalData["reason"] as String).contains("connection is not selected")
        }
    }

    private fun `then report should contain error about invalid connection`() {
        assertResultContains {
            it.category == SlackBuildFeatureHealthReport.invalidBuildFeatureCategory &&
                    (it.additionalData["reason"] as String).contains("connection with id")
        }
    }

    private fun `then report should contain error about invalid channel`() {
        assertResultContains {
            it.category == SlackBuildFeatureHealthReport.invalidBuildFeatureCategory &&
                    (it.additionalData["reason"] as String).contains("find channel #invalid_channel")
        }
    }

    private fun `then report should contain error about not being added to channel`() {
        assertResultContains {
            it.category == SlackBuildFeatureHealthReport.invalidBuildFeatureCategory &&
                    (it.additionalData["reason"] as String).contains("should be added to the channel")
        }
    }
}