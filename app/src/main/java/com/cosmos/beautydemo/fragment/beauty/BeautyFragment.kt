package com.cosmos.beautydemo.fragment.beauty

import android.view.View
import android.widget.CheckBox
import android.widget.SeekBar
import com.cosmos.beauty.module.beauty.IBeautyModule
import com.cosmos.beauty.module.beauty.SimpleBeautyType
import com.cosmos.beautydemo.R
import com.cosmos.beautydemo.recycleadapter.BaseFragment

class BeautyFragment : BaseFragment() {
    private lateinit var seekBar: SeekBar
    private lateinit var cbBigEye: CheckBox
    private lateinit var cbSkinWhite: CheckBox
    private lateinit var cbSKin: CheckBox
    private lateinit var cbThinFace: CheckBox
    private lateinit var cbRuddy: CheckBox
    private lateinit var rootView: View


    private var beautyModule: IBeautyModule? = null

    private fun changeValue(value: Float) {
        if (cbBigEye.isChecked) {
            beautyModule?.setValue(SimpleBeautyType.BIG_EYE, value)
        }
        if (cbSkinWhite.isChecked) {
            beautyModule?.setValue(SimpleBeautyType.SKIN_WHITENING, value)
        }
        if (cbSKin.isChecked) {
            beautyModule?.setValue(SimpleBeautyType.SKIN_SMOOTH, value)
        }
        if (cbThinFace.isChecked) {
            beautyModule?.setValue(SimpleBeautyType.THIN_FACE, value)
        }
        if (cbRuddy.isChecked) {
            beautyModule?.setValue(SimpleBeautyType.RUDDY, value)
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_beauty
    }

    override fun initView(view: View?) {
        rootView = view!!
        view?.let {
            seekBar = it.findViewById(R.id.seekBar)
            cbBigEye = it.findViewById(R.id.cbBigEye)
            cbSkinWhite = it.findViewById(R.id.cbSkinWhite)
            cbSKin = it.findViewById(R.id.cbSKin)
            seekBar = it.findViewById(R.id.seekBar)
            cbThinFace = it.findViewById(R.id.cbThinFace)
            cbRuddy = it.findViewById(R.id.cbRuddy)
        }
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                changeValue(progress / 100.0f)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    fun setBeautyModule(beautyModule: IBeautyModule) {
        this.beautyModule = beautyModule
    }

    fun setVisible(visible: Boolean) {
        rootView.visibility = (if (visible) View.VISIBLE else View.GONE)
    }
}