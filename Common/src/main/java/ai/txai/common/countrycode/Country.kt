package ai.txai.common.countrycode

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.lang.StringBuilder

class Country {

    @Expose
    @SerializedName("calling_codes")
    private var countryCode: ArrayList<String>? = null

    @Expose
    @SerializedName("first_char")
    private var firstLetter = ""

    @Expose
    @SerializedName("country_id")
    private var id = 0L

    @Expose
    @SerializedName("is_pop")
    private var isPop = false

    @Expose
    @SerializedName("iso_code")
    private var isoCode = ""

    @Expose
    @SerializedName("name")
    private var countryName = ""

    @Expose
    @SerializedName("flag")
    private var flag = ""

    fun getId() = id

    fun getName() = countryName

    fun getPhoneCode(): String {
        return countryCode?.let { it[0] } ?: ""
    }

    fun getIsoCode() = isoCode

    fun isPop() = isPop

    fun getFlag() = flag

    fun getFirstLetter() = firstLetter

    fun setFirstLetter(letter: String) {
        firstLetter = letter
    }

    fun copyAllValues(country: Country) {
        this.countryName = country.countryName
        this.countryCode = country.countryCode
        this.flag = country.flag
        this.isPop = country.isPop
        this.id = country.id
        this.firstLetter = country.firstLetter
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()
        return stringBuilder
            .append(", id: ")
            .append(id)
            .append(", name: ")
            .append(countryName)
            .append(", phoneCode: ")
            .append(countryCode)
            .toString()
    }

}