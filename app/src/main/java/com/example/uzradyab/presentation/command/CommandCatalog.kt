package com.example.uzradyab.presentation.command

import com.example.uzradyab.core.utils.FormatUtils.toPersianDigits

/** Visual tone of a command's icon circle. */
enum class CommandTone { Normal, Disable, Danger }

/** Icon key for a command; mapped to an ImageVector in the UI layer. */
enum class CommandIcon { Status, Where, Admin, FuelRelay, Restart, Server, Time, Lbs, Speed, ParkedMove, PowerCut, Battery, SosNumbers, Call }

data class NotifyOption(val value: String, val label: String)

/** A value the user has to provide before a command can be sent. Keys match `{key}` in the template. */
sealed interface CommandParam {
    val key: String
}

data class NumberParam(
    override val key: String,
    val label: String,
    val unit: String?,
    val min: Int,
    val max: Int,
) : CommandParam {
    val placeholder: String get() = "($min تا $max)".toPersianDigits()
}

data class NotifyParam(
    override val key: String,
    val options: List<NotifyOption>,
) : CommandParam {
    val label: String get() = "نوع اطلاع‌رسانی"
}

/** One SOS phone row. All phone params of a command are joined into [PHONES_PLACEHOLDER]. */
data class PhoneParam(
    override val key: String,
    val index: Int,
    val required: Boolean,
) : CommandParam

const val PHONES_PLACEHOLDER = "{phones}"

private val NOTIFY_INTERNET = NotifyOption("0", "فقط اینترنت")
private val NOTIFY_INTERNET_SMS = NotifyOption("1", "اینترنت و پیامک")
private val NOTIFY_INTERNET_CALL = NotifyOption("2", "اینترنت و تماس")
private val NOTIFY_ALL = NotifyOption("3", "اینترنت، پیامک و تماس")

val NOTIFY_BASIC = listOf(NOTIFY_INTERNET, NOTIFY_INTERNET_SMS)
val NOTIFY_FULL = listOf(NOTIFY_INTERNET, NOTIFY_INTERNET_SMS, NOTIFY_INTERNET_CALL, NOTIFY_ALL)

val COMMAND_TYPES = listOf(
    CommandData("status", "وضعیت دستگاه", "با ارسال این دستور دستگاه وضعیت کلی خود را پاسخ می‌دهد.", "STATUS#", icon = CommandIcon.Status),
    CommandData("where", "موقعیت فعلی دستگاه", "", "WHERE#", icon = CommandIcon.Where),
    CommandData("admin", "تعریف مدیر", "تعریف شماره خاص جهت دستور به دستگاه.", "SOS,A,{EMAIL}#", icon = CommandIcon.Admin),
    CommandData("start", "فعال کردن رله سوخت", "ارسال دستور وصل کردن سوخت خودرو در صورتی که سوخت از قبل توسط رله قطع شده باشد.", "RELAY,0#", icon = CommandIcon.FuelRelay),
    CommandData("stop", "غیر فعال کردن رله سوخت", "ارسال دستور قطع سوخت خودرو توسط رله در صورتی که سرعت خودرو کمتر از 10 کیلومتر باشد.", "RELAY,1#", icon = CommandIcon.FuelRelay, tone = CommandTone.Danger),
    CommandData("reset", "راه اندازی مجدد", "با ارسال این دستور دستگاه خاموش و مجدد راه اندازی می شود.", "RESET#", icon = CommandIcon.Restart, tone = CommandTone.Danger),
    CommandData("server", "تنظیم سرور", "ارسال این دستور سرور دستگاه را تنظیم کرده و مناسب است برای زمانی که دستگاه تازه فعال می‌شود.", "SERVER,1,uzradyab.ir,5023,0#", icon = CommandIcon.Server),
    CommandData("time", "تنظیم زمان دستگاه", "ارسال این دستور زمان دستگاه را تنظیم کرده و مناسب است برای زمانی که دستگاه تازه فعال می‌شود.", "GMT,E,0,0#", icon = CommandIcon.Time),
    CommandData("lbs", "تنظیم \u202ALBS\u202C", "تنظیم \u202ALBS\u202C", "LBSON#", icon = CommandIcon.Lbs),
    CommandData(
        "speed_on", "فعال‌سازی سرعت غیرمجاز", "", "SPEED,ON,{sec},{kmh},{m}#",
        icon = CommandIcon.Speed,
        params = listOf(
            NumberParam("kmh", "حد سرعت", "km/h", 1, 255),
            NumberParam("sec", "چرخه اطلاع‌رسانی", "ثانیه", 5, 600),
            NotifyParam("m", NOTIFY_BASIC),
        ),
    ),
    CommandData("speed_off", "غیرفعال‌سازی سرعت غیرمجاز", "", "SPEED,OFF#", icon = CommandIcon.Speed, tone = CommandTone.Disable),
    CommandData(
        "moving_on", "فعال‌سازی جابه‌جایی خودروی پارک‌شده", "", "MOVING,ON,{r},{m}#",
        icon = CommandIcon.ParkedMove,
        params = listOf(
            NumberParam("r", "محدوده", "متر", 100, 1000),
            NotifyParam("m", NOTIFY_FULL),
        ),
    ),
    CommandData("moving_off", "غیرفعال‌سازی جابه‌جایی خودروی پارک‌شده", "", "MOVING,OFF#", icon = CommandIcon.ParkedMove, tone = CommandTone.Disable),
    CommandData(
        "power_on", "فعال‌سازی هشدار قطع برق", "", "POWERALM,ON,{m},5,300#",
        icon = CommandIcon.PowerCut,
        params = listOf(NotifyParam("m", NOTIFY_FULL)),
    ),
    CommandData("power_off", "غیرفعال‌سازی هشدار قطع برق", "", "POWERALM,OFF#", icon = CommandIcon.PowerCut, tone = CommandTone.Disable),
    CommandData(
        "battery_on", "فعال‌سازی هشدار ضعف باتری", "", "BATALM,ON,{m}#",
        icon = CommandIcon.Battery,
        params = listOf(NotifyParam("m", NOTIFY_FULL)),
    ),
    CommandData("battery_off", "غیرفعال‌سازی هشدار ضعف باتری", "", "BATALM,OFF#", icon = CommandIcon.Battery, tone = CommandTone.Disable),
    CommandData(
        "sos_on", "فعال‌سازی شماره‌های اضطراری", "", "SOS,A,$PHONES_PLACEHOLDER#",
        icon = CommandIcon.SosNumbers,
        params = listOf(
            PhoneParam("n1", 1, required = true),
            PhoneParam("n2", 2, required = false),
            PhoneParam("n3", 3, required = false),
        ),
    ),
    CommandData("sos_off", "غیرفعال‌سازی شماره‌های اضطراری", "", "SOS,D,1,2,3#", icon = CommandIcon.SosNumbers, tone = CommandTone.Disable),
    CommandData(
        "call_on", "فعال‌سازی تماس اضطراری", "", "CALL,{n}#",
        icon = CommandIcon.Call,
        params = listOf(NumberParam("n", "تعداد تماس", null, 1, 3)),
    ),
    CommandData("call_off", "غیرفعال‌سازی تماس اضطراری", "", "CALL,0#", icon = CommandIcon.Call, tone = CommandTone.Disable),
)

data class CommandData(
    val type: String,
    val title: String,
    val description: String,
    val command: String,
    val icon: CommandIcon = CommandIcon.Status,
    val tone: CommandTone = CommandTone.Normal,
    val params: List<CommandParam> = emptyList(),
) {
    val needsValues: Boolean get() = params.isNotEmpty()
}

/**
 * Fills the `{key}` placeholders of [CommandData.command] from [values].
 * Numbers are sent without leading zeros; only filled phone numbers are joined.
 * `{EMAIL}` is left untouched for the existing send path.
 */
fun CommandData.buildCommand(values: Map<String, String>): String {
    var text = command
    params.forEach { param ->
        val raw = CommandValidation.normalizeDigits(values[param.key].orEmpty()).trim()
        when (param) {
            is NumberParam -> text = text.replace("{${param.key}}", raw.toIntOrNull()?.toString() ?: raw)
            is NotifyParam -> text = text.replace("{${param.key}}", raw)
            is PhoneParam -> Unit
        }
    }
    val phones = params.filterIsInstance<PhoneParam>()
    if (phones.isNotEmpty()) {
        val filled = phones.mapNotNull { phone ->
            CommandValidation.normalizeDigits(values[phone.key].orEmpty()).trim().takeIf { it.isNotEmpty() }
        }
        text = text.replace(PHONES_PLACEHOLDER, filled.joinToString(","))
    }
    return text
}
