package org.mint.android.rule.input

import android.text.InputType
import com.github.javafaker.Faker
import org.mint.android.Action
import org.mint.android.AndroidState
import org.mint.android.rule.BasicRules
import org.mint.android.rule.MultiplicativeRule
import org.mint.android.rule.input.datetime.DateInputSupplier
import org.mint.android.rule.input.datetime.TimeInputSupplier
import org.w3c.dom.Element
import java.math.BigDecimal
import java.util.Locale

object InputRules {

    fun xqueryRegexInputRule(description: String, pred: String, prio: String, reg: String): GenericInputRule =
        GenericInputRule(description, BasicRules.xpred(pred), BasicRules.xprio(prio), rgen(reg))

    // https://developer.android.com/reference/android/widget/TextView.html#attr_android:inputType
    // todo: the list of input types below is not exhaustive, more combinations are possible, e.g. text|textCapCharacters, textNoSuggestions|textVisiblePassword, textCapWords|textAutoComplete etc.
    private const val INPUT_TYPE_NONE = InputType.TYPE_NULL
    private const val INPUT_TYPE_TEXT = InputType.TYPE_CLASS_TEXT
    private const val INPUT_TYPE_NUMBER = InputType.TYPE_CLASS_NUMBER
    private const val INPUT_TYPE_NUMBER_DECIMAL = INPUT_TYPE_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
    private const val INPUT_TYPE_NUMBER_SIGNED = INPUT_TYPE_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
    private const val INPUT_TYPE_NUMBER_PASSWORD = INPUT_TYPE_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
    private const val INPUT_TYPE_EMAIL_ADDRESS = INPUT_TYPE_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
    private const val INPUT_TYPE_PERSON_NAME = INPUT_TYPE_TEXT or InputType.TYPE_TEXT_VARIATION_PERSON_NAME
    private const val INPUT_TYPE_TEXT_MULTILINE = INPUT_TYPE_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
    private const val INPUT_TYPE_TEXT_PASSWORD = INPUT_TYPE_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
    private const val INPUT_TYPE_URI = INPUT_TYPE_TEXT or InputType.TYPE_TEXT_VARIATION_URI
    private const val INPUT_TYPE_DATETIME = InputType.TYPE_CLASS_DATETIME
    private const val INPUT_TYPE_PHONE = InputType.TYPE_CLASS_PHONE
    private const val INPUT_TYPE_DATE = INPUT_TYPE_DATETIME or InputType.TYPE_DATETIME_VARIATION_DATE
    private const val INPUT_TYPE_TIME = INPUT_TYPE_DATETIME or InputType.TYPE_DATETIME_VARIATION_TIME
    private const val INPUT_TYPE_POSTAL_ADDRESS = INPUT_TYPE_TEXT or InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS

    private val multiByteUTF8 = listOf(
        "5򥈼һ嫄󕼉&gt;[斊һ𖁊󅯨K𪥶لH򠍩5ˌڏྰȞ񡸄󺌡򋭮熥!뫭Ŷށ·򊞖Ӎ",
        "ﾔP􌸎潮𤿷㓪ܮ񾂧𲔥󫅴ۑЦ+僑𘊣'oճ^񎡭.8畠Ҥ򻙼࠹ﺕ뻷Ƽᰧ脯",
        "٤󘳷4숓➃񡾟k𡓯ރÒ̋򅥿y⬰LۑӎOξ樲䵽Ԝ̻샽e䐎ɰ퟾󬌇ýv򄬓",
        "𥒪䔪𢂚񽖐d󷗜ᶟ񢡖𗼇򩒰򪯠󒐦ٛՄ󑽘í䀑򴚼􊂺􄘙گW򻓧Ꝋ񧆌񿒖⁢w뀯Ӄ&lt;䵖",
        "󧓇󔰣幽Я󂖇9񳴎􋞂,ی񗛳ٸ٥lŘu[⻠sŏK]ʐ􀀛󶚠󩙲}᷼􈔌p"
    )

    private val twoByteUTF8 = listOf(
        "݁ܚӒ̩݄֕ζ˅̱̂ϒŉÍĜϲݒ",
        "ܶؿ̰߶ՏҮȍđǮҖȔʖִŽƫǻ",
        "ސմ֌ȬƗŵިֆ݈ـŎӟůݑױǬ",
        "˘ҟӀ͍̀ߧʆђԿ̒ʲع˷ѭݢҒ",
        "ߢІŋ߹źѩȱϻڈǶձǮ׸̀ڧܨ"
    )

    fun rgen(r: String) = RegexInputGenerator(r)
    private val defaultLocale = Locale("nl")

    private val locale: (AndroidState) -> Locale = { s ->
        val l = (s.node as Element).getAttribute("locale")

        if (l.isNullOrBlank()) {
            defaultLocale
        } else {
            Locale(l)
        }
    }

    private const val standardEditTextPredicate = " @isEditText = 'true' and @isDisplayed = 'true' " +
        "and @hasOnClickListeners = 'false'"

    fun defaultUTF8InputRule(): PositionBasedInputRule =
        PositionBasedInputRule(
            description = "Generate UTF8 text streams for anything accepting text",
            pred = BasicRules.xpred(".[$standardEditTextPredicate]"),
            prio = BasicRules.defaultPrio,
            gen = {
                it.selectOneOf(
                    it.selectOneOf(
                        listOf(
                            multiByteUTF8,
                            twoByteUTF8
                        )
                    )
                )
            },
            itemPosition = BasicRules.positionInViewHierarchy
        )

    fun defaultTextInputRule(): PositionBasedInputRule =
        PositionBasedInputRule(
            description = "Generate generic text for anything accepting text",
            pred = BasicRules.xpred(
                ".[" +
                    standardEditTextPredicate +
                    " and number(@inputType) = $INPUT_TYPE_TEXT or number(@inputType) = $INPUT_TYPE_TEXT_PASSWORD ]"
            ),
            prio = BasicRules.defaultPrio,
            gen = rgen("([A-Za-z ]{5,20}|([A-Za-z0-9 ]{5,20})|([0-9]{1,5})"),
            itemPosition = BasicRules.positionInViewHierarchy
        )

    fun defaultMultilineTextInputRule(): PositionBasedInputRule =
        PositionBasedInputRule(
            description = "Generate generic text for anything accepting text",
            pred = BasicRules.xpred(
                ".[" +
                    standardEditTextPredicate +
                    "and number(@inputType) = $INPUT_TYPE_TEXT_MULTILINE ]"
            ),
            prio = BasicRules.defaultPrio,
            gen = rgen("([A-Za-z \\n]{5,40}|([A-Za-z0-9 \\n]{5,40})|([0-9]{1,5})"),
            itemPosition = BasicRules.positionInViewHierarchy
        )

    fun defaultEmailAddressInputRule(): GenericInputRule =
        GenericInputRule(
            description = "Generate text in email address format for widgets accepting email addresses",
            pred = BasicRules.xpred(
                ".[" +
                    standardEditTextPredicate +
                    "and number(@inputType)= $INPUT_TYPE_EMAIL_ADDRESS]"
            ),
            prio = BasicRules.defaultPrio,
            gen = { s ->
                // UTF-8 characters are permitted by mail servers supporting internationalized email
                val plantEmoji = String(byteArrayOf(0xF0.toByte(), 0x9F.toByte(), 0x8C.toByte(), 0xBF.toByte()))
                rgen(
                    "(" +
                        "$plantEmoji|" +
                        "[A-Za-z0-9_.-]{3,64}|" +
                        "[a-zA-Z0-9!#%+/=^`*&\$\'{|}_.~-]{3,64})" +
                        "@[A-Za-z0-9]{3,64}\\.(nl|com|net|org|co\\.uk)"
                )
                    .invoke(s)
            }
        )

    fun defaultNumberInputRule(): GenericInputRule =
        GenericInputRule(
            description = "Generate number input for widgets accepting input of numbers type",
            pred = BasicRules.xpred(
                ".[" +
                    standardEditTextPredicate +
                    "and number(@inputType)= $INPUT_TYPE_NUMBER or number(@inputType) = $INPUT_TYPE_NUMBER_PASSWORD ]"
            ),
            prio = BasicRules.defaultPrio,
            gen = rgen("[0-9]{1,6}")
        )

    fun defaultDecimalNumberInputRule(): GenericInputRule =
        GenericInputRule(
            description = "Generate decimal number input for widgets accepting input of decimal number type",
            pred = BasicRules.xpred(
                ".[" +
                    standardEditTextPredicate +
                    "and number(@inputType)= $INPUT_TYPE_NUMBER_DECIMAL]"
            ),
            prio = BasicRules.defaultPrio,
            gen = rgen("[1-9]{0,2}\\.?[0-9]{1,2}|0?\\.[0-9]{1,4}")
        )

    fun defaultSignedNumberInputRule(): GenericInputRule =
        GenericInputRule(
            description = "Generate signed number input for widgets accepting input of signed number type",
            pred = BasicRules.xpred(
                ".[" +
                    standardEditTextPredicate +
                    "and number(@inputType)= $INPUT_TYPE_NUMBER_SIGNED]"
            ),
            prio = BasicRules.defaultPrio,
            gen = rgen("[-+]{0,1}[1-9]{1,6}")
        )

    fun defaultPersonNameInputRule(): GenericInputRule =
        GenericInputRule(
            description = "Generate text for widgets accepting person name input",
            pred = BasicRules.xpred(
                ".[" +
                    standardEditTextPredicate +
                    "and number(@inputType)= $INPUT_TYPE_PERSON_NAME]"
            ),
            prio = BasicRules.defaultPrio,
            // allows single-letter last name
            // capitalized single-letter first names can occur when using initials only
            gen = rgen("[A-Z]{1}[a-z]{0,32} [A-Z]{1}[a-z]{0,32}")
        )

    fun defaultUriRule(): GenericInputRule =
        GenericInputRule(
            description = "Generate text for widgets accepting input of uri type",
            pred = BasicRules.xpred(
                ".[" +
                    standardEditTextPredicate +
                    "and number(@inputType) = $INPUT_TYPE_URI]"
            ),
            prio = BasicRules.defaultPrio,
            gen = { "https://ing.nl" }
        )

    fun defaultPhoneNumberInputRule(): GenericInputRule =
        GenericInputRule(
            description = "Generate phone number for widgets accepting text in phone format",
            pred = BasicRules.xpred(
                ".[" +
                    standardEditTextPredicate +
                    "and number(@inputType) = $INPUT_TYPE_PHONE]"
            ),
            prio = BasicRules.defaultPrio,
            gen = { s ->
                Faker(locale.invoke(s)).phoneNumber().phoneNumber()
            }
        )

    fun defaultPostalAddressInputRule(): GenericInputRule =
        GenericInputRule(
            description = "Generate postal address input for widgets accepting input as postal address",
            pred = BasicRules.xpred(
                ".[" +
                    standardEditTextPredicate +
                    "and number(@inputType) = $INPUT_TYPE_POSTAL_ADDRESS]"
            ),
            prio = BasicRules.defaultPrio,
            gen = { s ->
                Faker(locale.invoke(s)).address().zipCode()
            }
        )

    fun defaultDateInputRule(): GenericInputRule =
        GenericInputRule(
            description = "Generate date input for widgets accepting input as date",
            pred = BasicRules.xpred(
                ".[" +
                    standardEditTextPredicate +
                    "and number(@inputType) = $INPUT_TYPE_DATE]"
            ),
            prio = BasicRules.fprio(BigDecimal(1)),
            gen = { s -> DateInputSupplier.get(s.rnd, locale.invoke(s)) }
        )

    fun defaultTimeInputRule(): GenericInputRule =
        GenericInputRule(
            description = "Generate time input for widgets accepting input as time",
            pred = BasicRules.xpred(".[" + standardEditTextPredicate + "and number(@inputType) = $INPUT_TYPE_TIME]"),
            prio = BasicRules.defaultPrio,
            gen = { s -> TimeInputSupplier.get(s.rnd, locale.invoke(s)) }
        )

    // for input types & combinations not covered by the more specific rules
    fun defaultGenericTextInputRule(): PositionBasedInputRule =
        PositionBasedInputRule(
            description = "Generate generic text for anything accepting any type of input",
            pred = BasicRules.xpred(".[$standardEditTextPredicate]"),
            prio = BasicRules.fprio(BigDecimal(0.5)),
            gen = rgen("([A-Za-z ]{5,20}|([A-Za-z0-9 ]{5,20})|([0-9]{1,5})"),
            itemPosition = BasicRules.positionInViewHierarchy
        )

    fun defaultUneditableTextClickDeprioritizeRule(): MultiplicativeRule =
        MultiplicativeRule(
            description = "De-prioritized the input of text in text fields that are uneditable",
            action = Action.INPUT,
            pred = BasicRules.xpred(".[" + standardEditTextPredicate + "and number(@inputType) = $INPUT_TYPE_NONE]"),
            prio = BasicRules.fprio(BigDecimal(0.1))
        )

    fun defaultTextClickDeprioritizeRule(): MultiplicativeRule =
        MultiplicativeRule(
            description = "De-prioritized the clicking of text elements",
            action = Action.CLICK,
            pred = BasicRules.xpred(".[$standardEditTextPredicate]"),
            prio = BasicRules.fprio(BigDecimal(0.1))
        )

    fun defaultTextClickAtPositionDeprioritizeRule(): MultiplicativeRule =
        MultiplicativeRule(
            description = "De-prioritized the clicking of text elements",
            action = Action.CLICK_ON_ITEM_AT_POSITION,
            pred = BasicRules.xpred(".[$standardEditTextPredicate]"),
            prio = BasicRules.fprio(BigDecimal(0.1))
        )
}
