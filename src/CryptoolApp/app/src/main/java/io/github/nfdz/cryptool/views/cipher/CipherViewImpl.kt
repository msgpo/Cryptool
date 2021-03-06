package io.github.nfdz.cryptool.views.cipher

import android.content.Context
import android.view.View
import io.github.nfdz.cryptool.R
import io.github.nfdz.cryptool.common.utils.ClipboardHelper
import io.github.nfdz.cryptool.common.utils.isNightUiMode
import io.github.nfdz.cryptool.common.utils.toast
import io.github.nfdz.cryptool.common.widgets.InputTextBoxView
import io.github.nfdz.cryptool.common.widgets.OutputTextBoxView

class CipherViewImpl(private val view: View?, private val context: Context?) : CipherContract.View {

    private val presenter: CipherContract.Presenter by lazy {
        CipherPresenterImpl(this, context?.let { CipherInteractorImpl(it) })
    }

    private var cipher_itb_pass: InputTextBoxView? = null
    private var cipher_itb_origin: InputTextBoxView? = null
    private var cipher_otb_processed: OutputTextBoxView? = null
    private var cipher_btn_reverse: View? = null

    private var mode: CipherContract.ModeFlag = CipherContract.ModeFlag.ENCRYIPT_MODE

    override fun onViewCreated() {
        bindView()
        setupView()
        presenter.onCreate()
    }

    override fun onDestroyView() {
        presenter.onDestroy()
        cipher_itb_pass = null
        cipher_itb_origin = null
        cipher_otb_processed = null
        cipher_btn_reverse = null
    }

    private fun bindView() {
        cipher_itb_pass = view?.findViewById(R.id.cipher_itb_pass)
        cipher_itb_origin = view?.findViewById(R.id.cipher_itb_origin)
        cipher_otb_processed = view?.findViewById(R.id.cipher_otb_processed)
        cipher_btn_reverse = view?.findViewById(R.id.cipher_btn_reverse)
    }

    private fun setupView() {
        if (context.isNightUiMode() == true) {
            cipher_itb_pass?.setupView(
                R.color.colorDark,
                R.drawable.selector_action_dark,
                R.color.colorLight,
                R.drawable.ic_passphrase,
                R.string.cipher_passphrase_label
            )
        } else {
            cipher_itb_pass?.setupView(
                R.color.colorLight,
                R.drawable.selector_action_light,
                R.color.colorDark,
                R.drawable.ic_passphrase,
                R.string.cipher_passphrase_label
            )
        }
        setupTextBoxesWithMode()
        setupActions()
        setupInputListeners()
    }

    private fun setupTextBoxesWithMode() {
        when (mode) {
            CipherContract.ModeFlag.ENCRYIPT_MODE -> {
                setupOriginBox(true)
                setupProcessedBox(true)
            }
            CipherContract.ModeFlag.DECRYIPT_MODE -> {
                setupProcessedBox(false)
                setupOriginBox(false)
            }
        }
    }

    private fun setupOriginBox(encryptMode: Boolean) {
        if (encryptMode) {
            val actionColor =
                if (context.isNightUiMode() == true) {
                    cipher_itb_origin?.setupView(
                        R.color.colorDark,
                        R.drawable.selector_action_dark,
                        R.color.colorLight,
                        R.drawable.ic_no_encryption,
                        R.string.cipher_plain_label
                    )
                    R.color.colorLight
                } else {
                    cipher_itb_origin?.setupView(
                        R.color.colorLight,
                        R.drawable.selector_action_light,
                        R.color.colorDark,
                        R.drawable.ic_no_encryption,
                        R.string.cipher_plain_label
                    )
                    R.color.colorDark
                }
            cipher_itb_origin?.setupAction1Icon(R.drawable.ic_copy, actionColor)
            cipher_itb_origin?.setupAction2Icon(R.drawable.ic_paste, actionColor)
            cipher_itb_origin?.setupAction3Icon(R.drawable.ic_clear, actionColor)
        } else {
            cipher_itb_origin?.setupView(
                R.color.colorDark,
                R.drawable.selector_action_dark,
                R.color.colorLight,
                R.drawable.ic_encryption,
                R.string.cipher_encrypted_label
            )
            cipher_itb_origin?.setupAction1Icon(R.drawable.ic_copy, R.color.colorLight)
            cipher_itb_origin?.setupAction2Icon(R.drawable.ic_paste, R.color.colorLight)
            cipher_itb_origin?.setupAction3Icon(R.drawable.ic_clear, R.color.colorLight)
        }
    }

    private fun setupProcessedBox(encryptMode: Boolean) {
        if (encryptMode) {
            cipher_otb_processed?.setupView(
                R.color.colorDark,
                R.drawable.selector_action_dark,
                R.color.colorLight,
                R.drawable.ic_encryption,
                R.string.cipher_encrypted_label
            )
            cipher_otb_processed?.setupAction1Icon(R.drawable.ic_copy, R.color.colorLight)
            cipher_otb_processed?.setupAction2Icon(
                R.drawable.ic_info_outline,
                R.color.colorLight
            )
        } else {
            val actionColor = if (context.isNightUiMode() == true) {
                cipher_otb_processed?.setupView(
                    R.color.colorDark,
                    R.drawable.selector_action_dark,
                    R.color.colorLight,
                    R.drawable.ic_no_encryption,
                    R.string.cipher_plain_label
                )
                R.color.colorLight
            } else {
                cipher_otb_processed?.setupView(
                    R.color.colorLight,
                    R.drawable.selector_action_light,
                    R.color.colorDark,
                    R.drawable.ic_no_encryption,
                    R.string.cipher_plain_label
                )
                R.color.colorDark
            }
            cipher_otb_processed?.setupAction1Icon(R.drawable.ic_copy, actionColor)
            cipher_otb_processed?.setupAction2Icon(
                R.drawable.ic_info_outline,
                actionColor
            )
        }
    }

    private fun setupActions() {
        cipher_itb_pass?.setupAction3Icon(
            R.drawable.ic_clear, if (context.isNightUiMode() == true) {
                R.color.colorLight
            } else {
                R.color.colorDark
            }
        )
        cipher_itb_pass?.setupAction1 { presenter.onViewPassphraseClick() }
        cipher_itb_pass?.setupAction2 { presenter.onLockPassphraseClick() }
        cipher_itb_pass?.setupAction3 {
            cipher_itb_pass?.setText("")
            presenter.onPassphraseTextChanged()
        }
        cipher_itb_origin?.setupAction1 {
            context?.let {
                ClipboardHelper.copyText(
                    it,
                    cipher_itb_origin?.getText() ?: ""
                )
            }
        }
        cipher_itb_origin?.setupAction2 {
            context?.let {
                ClipboardHelper.pasteText(it) { pasteText ->
                    cipher_itb_origin?.setText(pasteText)
                    presenter.onOriginTextChanged()
                }
            }
        }
        cipher_itb_origin?.setupAction3 {
            cipher_itb_origin?.setText("")
            presenter.onOriginTextChanged()
        }
        cipher_otb_processed?.setupAction1 {
            context?.let {
                ClipboardHelper.copyText(
                    it,
                    cipher_otb_processed?.getText() ?: ""
                )
            }
        }
        cipher_otb_processed?.setupAction2 {
            context?.toast(R.string.cipher_info)
        }
        cipher_btn_reverse?.setOnClickListener {
            presenter.onToggleModeClick()
        }
    }

    private fun setupInputListeners() {
        cipher_itb_pass?.setInputChangedListener {
            presenter.onPassphraseTextChanged()
        }
        cipher_itb_origin?.setInputChangedListener {
            presenter.onOriginTextChanged()
        }
    }

    override fun getCipherMode(): CipherContract.ModeFlag {
        return mode
    }

    override fun setCipherMode(mode: CipherContract.ModeFlag) {
        this.mode = mode
        setupTextBoxesWithMode()
    }

    override fun getOriginText(): String {
        return cipher_itb_origin?.getText() ?: ""
    }

    override fun setOriginText(text: String) {
        cipher_itb_origin?.setText(text)
    }

    override fun getProcessedText(): String {
        return cipher_otb_processed?.getText() ?: ""
    }

    override fun setProcessedText(text: String) {
        cipher_otb_processed?.setText(text)
    }

    override fun getPassphrase(): String {
        return cipher_itb_pass?.getText() ?: ""
    }

    override fun setPassphrase(pass: String) {
        cipher_itb_pass?.setText(pass)
    }

    override fun setPassphraseMode(visible: Boolean, enabled: Boolean) {
        val iconColor =
            if (context.isNightUiMode() == true) {
                if (enabled) R.color.colorLight else R.color.colorSemiTransparentLight
            } else {
                if (enabled) R.color.colorDark else R.color.colorSemiTransparentDark
            }
        cipher_itb_pass?.setupAction2Icon(R.drawable.ic_save, iconColor)
        if (visible) {
            cipher_itb_pass?.setupAction1Icon(R.drawable.ic_eye_blind, iconColor)
            cipher_itb_pass?.setInputTypePassword(visible = true)
        } else {
            cipher_itb_pass?.setupAction1Icon(R.drawable.ic_eye, iconColor)
            cipher_itb_pass?.setInputTypePassword(visible = false)
        }
        cipher_itb_pass?.setInputEnabled(enabled)
        cipher_itb_pass?.setAction1Enabled(enabled)
        cipher_itb_pass?.setAction2Enabled(enabled)
    }

}