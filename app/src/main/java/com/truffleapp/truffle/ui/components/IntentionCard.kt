package com.truffleapp.truffle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truffleapp.truffle.data.SampleData
import com.truffleapp.truffle.data.pickReflection
import java.time.LocalDate
import com.truffleapp.truffle.ui.theme.ColorSurface
import com.truffleapp.truffle.ui.theme.ColorTextSerifBody
import com.truffleapp.truffle.ui.theme.SerifFamily
import com.truffleapp.truffle.ui.theme.StillwaterTheme

// ── IntentionCard ─────────────────────────────────────────────────────────
// Reference spec (primitives.jsx):
//   background     ColorSurface (#EDE8DF)
//   borderRadius   14dp
//   padding        18dp vertical / 20dp horizontal
//   label          optional Caps at top, gap 10dp before body
//   body           serif italic 17sp, see [ColorTextSerifBody]

private val CardShape = RoundedCornerShape(14.dp)

@Composable
fun IntentionCard(
    body: String,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(ColorSurface)
            .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        if (label != null) {
            Caps(text = label)
            // 10dp gap between label and body (spacedBy would affect all children)
        }
        Text(
            text = "\u201C$body\u201D",  // wraps in typographic curly quotes
            modifier = if (label != null) Modifier.padding(top = 10.dp) else Modifier,
            style = TextStyle(
                fontFamily = SerifFamily,
                fontStyle = FontStyle.Italic,
                fontSize = 17.sp,
                color = ColorTextSerifBody,
                lineHeight = (17 * 1.55).sp,
            ),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1EB)
@Composable
private fun IntentionCardPreview() {
    StillwaterTheme {
        IntentionCard(
            label = "Today\u2019s reflection",
            body = pickReflection(SampleData, LocalDate.now()),
            modifier = Modifier.padding(16.dp),
        )
    }
}
