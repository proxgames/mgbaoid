package io.mgba.ui.views

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.mgba.Constants
import io.mgba.data.database.Game
import io.mgba.R
import io.mgba.ui.activities.EmulationActivity
import io.mgba.utilities.GlideUtils
import io.mgba.utilities.GlideUtils.Colors
import kotlinx.android.synthetic.main.library_sheet_view.*

class GameInformationView : BottomSheetDialogFragment(), View.OnClickListener {

    private var game: Game? = null

    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTheme
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.library_sheet_view, container, false)
        val arguments = arguments

        if (arguments != null)
            this.game = arguments.getParcelable(Constants.ARG_PLAY_GAME)

        prepareView()

        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState!!.putParcelable(Constants.ARG_SHEET_CONTENT, game)
    }

    private fun prepareView() {
        game_title_bs.text = game!!.getName()
        game_desc_bs.text = game!!.description

        if (game_cover_bs != null && game!!.coverURL != null) {
            GlideUtils.init(this, game!!.coverURL!!)
                    .setPlaceholders(R.drawable.placeholder, R.drawable.error)
                    .colorView(Colors.VIBRANT, Colors.DARK_MUTED, game_play_bs, game_savetitle_bs)
                    .colorView(Colors.LIGHT_MUTED, Colors.LIGHT_VIBRANT, game_text_content_bs, game_image_content_bs)
                    .colorViewWithCustomBackground(Colors.LIGHT_MUTED, Colors.LIGHT_VIBRANT, game_header_bs)
                    .colorView(Colors.LIGHT_VIBRANT, true, game_title_bs)
                    .colorView(Colors.LIGHT_VIBRANT, false, game_desc_bs)
                    .build(game_cover_bs)
        } else {
            //set header color back to normal
            val background = game_header_bs.background

            if (background is GradientDrawable) {
                background.setColor(resources.getColor(R.color.colorPrimary))

            }
        }
    }

    override fun onClick(v: View) {
        val it = Intent(context, EmulationActivity::class.java)
        it.putExtra(Constants.ARG_PLAY_GAME, game)
        context!!.startActivity(it)
    }

    companion object {

        fun newInstance(game: Game): GameInformationView {
            val args = Bundle()
            args.putParcelable(Constants.ARG_PLAY_GAME, game)

            val frag = GameInformationView()
            frag.arguments = args
            return frag
        }
    }
}