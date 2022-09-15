package ai.txai.commonbiz.data

import ai.txai.database.site.Site

class BizSite(private val site: Site, private val distance: Double) {

    fun getSite() = site

    fun getDistance() = distance
}