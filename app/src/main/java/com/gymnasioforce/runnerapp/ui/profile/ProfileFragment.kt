package com.gymnasioforce.runnerapp.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.gymnasioforce.runnerapp.R
import com.gymnasioforce.runnerapp.data.AchievementCalculator
import com.gymnasioforce.runnerapp.databinding.FragmentProfileBinding
import com.gymnasioforce.runnerapp.network.RetrofitClient
import com.gymnasioforce.runnerapp.ui.auth.LoginActivity
import com.gymnasioforce.runnerapp.utils.AvatarHelper
import com.gymnasioforce.runnerapp.utils.Prefs
import com.gymnasioforce.runnerapp.utils.showToast
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ProfileFragment : Fragment() {

    private lateinit var b: FragmentProfileBinding
    private val viewModel: ProfileViewModel by viewModels()

    private val pickPhoto = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { uploadPhoto(it) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        b = FragmentProfileBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        super.onViewCreated(view, state)
        setupCountryDropdown()
        setupThemeToggle()
        b.rvAchievements.layoutManager = LinearLayoutManager(requireContext())

        b.ivAvatar.setOnClickListener { pickPhoto.launch("image/*") }
        b.btnSave.setOnClickListener { saveProfile() }
        b.btnLogout.setOnClickListener { doLogout() }
        b.btnDeleteAccount.setOnClickListener { confirmDeleteAccount() }

        observeViewModel()
        viewModel.loadProfile()
    }

    private fun observeViewModel() {
        viewModel.user.observe(viewLifecycleOwner) { user ->
            user ?: return@observe
            b.etEditName.setText(user.name)
            b.actvEditCountry.setText(user.country)
            b.tvName.text = user.name.uppercase()
            b.tvCountry.text = user.country
            b.tvStatKm.text = "%.1f".format(user.totalKm)
            b.tvStatKcal.text = "${user.totalCalories}"
        }

        viewModel.totalRuns.observe(viewLifecycleOwner) { runs ->
            b.tvStatRuns.text = "$runs"
            val user = viewModel.user.value ?: return@observe
            loadAchievements(runs, user.totalKm, user.totalCalories)
        }

        viewModel.photoUrl.observe(viewLifecycleOwner) { url ->
            if (url != null) {
                Glide.with(this).load(url).circleCrop().into(b.ivAvatar)
            } else {
                val name = viewModel.user.value?.name ?: ""
                b.ivAvatar.setImageDrawable(AvatarHelper.generateInitials(requireContext(), name, 100))
            }
        }

        viewModel.message.observe(viewLifecycleOwner) { msg ->
            msg ?: return@observe
            when (msg) {
                "saved" -> {
                    showToast(getString(R.string.success_profile_updated))
                    val user = viewModel.user.value
                    user?.let {
                        Prefs(requireContext()).userName = it.name
                        b.tvName.text = it.name.uppercase()
                        b.tvCountry.text = it.country
                    }
                }
                "photo_ok" -> showToast(getString(R.string.success_photo_updated))
                "error_profile" -> showToast(getString(R.string.error_loading_profile))
                "error_save" -> showToast(getString(R.string.error_saving_profile))
                "error_photo" -> showToast(getString(R.string.error_uploading_photo))
                "error_delete" -> showToast(getString(R.string.error_deleting_account))
            }
            viewModel.clearMessage()
        }

        viewModel.accountDeleted.observe(viewLifecycleOwner) { deleted ->
            if (deleted) {
                Prefs(requireContext()).clear()
                RetrofitClient.clearToken()
                startActivity(Intent(requireContext(), LoginActivity::class.java))
                requireActivity().finishAffinity()
            }
        }
    }

    private fun setupCountryDropdown() {
        val countries = resources.getStringArray(R.array.countries)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, countries)
        b.actvEditCountry.setAdapter(adapter)
    }

    private fun setupThemeToggle() {
        val currentMode = Prefs(requireContext()).themeMode
        when (currentMode) {
            AppCompatDelegate.MODE_NIGHT_NO -> b.toggleTheme.check(R.id.btnThemeLight)
            AppCompatDelegate.MODE_NIGHT_YES -> b.toggleTheme.check(R.id.btnThemeDark)
            else -> b.toggleTheme.check(R.id.btnThemeSystem)
        }

        b.toggleTheme.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val mode = when (checkedId) {
                R.id.btnThemeLight -> AppCompatDelegate.MODE_NIGHT_NO
                R.id.btnThemeDark -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            Prefs(requireContext()).themeMode = mode
            AppCompatDelegate.setDefaultNightMode(mode)
        }
    }

    private fun loadAchievements(totalRuns: Int, totalKm: Double, totalCalories: Int) {
        val achievements = AchievementCalculator.calculate(totalRuns, totalKm, totalCalories)
        b.rvAchievements.adapter = AchievementAdapter(achievements)
        val unlocked = achievements.count { it.unlocked }
        b.tvAchievementsCount.text = getString(R.string.achievements_unlocked, unlocked, achievements.size)
    }

    private fun saveProfile() {
        val name = b.etEditName.text.toString().trim()
        val country = b.actvEditCountry.text.toString().trim()
        if (name.isEmpty() || country.isEmpty()) {
            showToast(getString(R.string.validation_complete_fields))
            return
        }
        viewModel.saveProfile(name, country)
    }

    private fun uploadPhoto(uri: Uri) {
        val stream = requireContext().contentResolver.openInputStream(uri) ?: return
        val file = File(requireContext().cacheDir, "photo.jpg")
        file.outputStream().use { stream.copyTo(it) }

        val body = MultipartBody.Part.createFormData(
            "photo", file.name, file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        )
        viewModel.uploadPhoto(body)
    }

    private fun confirmDeleteAccount() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_account_title))
            .setMessage(getString(R.string.delete_account_message))
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .setPositiveButton(getString(R.string.btn_confirm_delete)) { _, _ ->
                viewModel.deleteAccount()
            }
            .show()
    }

    private fun doLogout() {
        viewModel.logout()
    }
}
