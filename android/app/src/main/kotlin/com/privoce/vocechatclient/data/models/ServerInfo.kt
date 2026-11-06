package com.privoce.vocechatclient.data.models

import com.google.gson.annotations.SerializedName

data class ServerInfo(
    val name: String? = null,
    val description: String? = null,
    val version: String? = null,
    val logo: String? = null,
    @SerializedName("terms_of_service") val termsOfService: String? = null,
    @SerializedName("privacy_policy") val privacyPolicy: String? = null,
    @SerializedName("allow_registration") val allowRegistration: Boolean = true
)

data class ServerOrgInfo(
    val name: String? = null,
    val description: String? = null,
    val logo: String? = null
)

