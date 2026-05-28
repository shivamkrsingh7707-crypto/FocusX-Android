package com.focusx.app.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.focusx.app.R
import com.focusx.app.databinding.BottomSheetSettingsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SettingsBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetSettingsBinding? = null
    private val binding get() = _binding!!

    private var initialHaptic = true
    private var initialSound = true
    private var initialAura = true
    private var initialStrict = false

    var onSettingsChanged: ((haptic: Boolean, sound: Boolean, aura: Boolean, strict: Boolean) -> Unit)? = null

    override fun getTheme(): Int = R.style.Theme_FocusX_BottomSheet

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.hapticSwitch.isChecked = initialHaptic
        binding.soundSwitch.isChecked = initialSound
        binding.auraSwitch.isChecked = initialAura
        binding.strictModeSwitch.isChecked = initialStrict

        binding.root.startAnimation(
            AnimationUtils.loadAnimation(requireContext(), R.anim.fade_scale_in)
        )
    }

    override fun onDestroyView() {
        onSettingsChanged?.invoke(
            binding.hapticSwitch.isChecked,
            binding.soundSwitch.isChecked,
            binding.auraSwitch.isChecked,
            binding.strictModeSwitch.isChecked
        )
        _binding = null
        super.onDestroyView()
    }

    companion object {
        fun newInstance(
            haptic: Boolean,
            sound: Boolean,
            aura: Boolean,
            strict: Boolean
        ): SettingsBottomSheet {
            val sheet = SettingsBottomSheet()
            sheet.initialHaptic = haptic
            sheet.initialSound = sound
            sheet.initialAura = aura
            sheet.initialStrict = strict
            return sheet
        }
    }
}
