package com.gymnasioforce.runnerapp.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import androidx.recyclerview.widget.LinearLayoutManager
import com.gymnasioforce.runnerapp.R
import com.gymnasioforce.runnerapp.data.AchievementCalculator
import com.gymnasioforce.runnerapp.databinding.FragmentProfileBinding
import com.gymnasioforce.runnerapp.network.RetrofitClient
import com.gymnasioforce.runnerapp.ui.auth.LoginActivity
import com.gymnasioforce.runnerapp.utils.AvatarHelper
import com.gymnasioforce.runnerapp.utils.Prefs
import com.gymnasioforce.runnerapp.utils.showToast
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ProfileFragment : Fragment() {

    private lateinit var b: FragmentProfileBinding

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
        b.rvAchievements.layoutManager = LinearLayoutManager(requireContext())
        loadProfile()
        b.ivAvatar.setOnClickListener { pickPhoto.launch("image/*") }
        b.btnSave.setOnClickListener { saveProfile() }
        b.btnLogout.setOnClickListener { doLogout() }
    }

    private fun setupCountryDropdown() {
        val countries = resources.getStringArray(com.gymnasioforce.runnerapp.R.array.countries)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, countries)
        b.actvEditCountry.setAdapter(adapter)
    }

    private fun loadProfile() {
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.api.getProfile()
                resp.body()?.data?.let { user ->
                    b.etEditName.setText(user.name)
                    b.actvEditCountry.setText(user.country)
                    b.tvName.text = user.name.uppercase()
                    b.tvCountry.text = user.country
                    b.tvStatKm.text = "%.1f".format(user.totalKm)
                    b.tvStatKcal.text = "${user.totalCalories}"
                    // Cargar stats para logros
                    try {
                        val statsResp = RetrofitClient.api.getMonthlyStats()
                        val runs = statsResp.body()?.data?.totalRuns ?: 0
                        loadAchievements(runs, user.totalKm, user.totalCalories)
                    } catch (_: Exception) {
                        loadAchievements(0, user.totalKm, user.totalCalories)
                    }
                    if (user.photoUrl != null) {
                        Glide.with(this@ProfileFragment).load(user.photoUrl).circleCrop().into(b.ivAvatar)
                    } else {
                        b.ivAvatar.setImageDrawable(AvatarHelper.generateInitials(requireContext(), user.name, 100))
                    }
                }
            } catch (e: Exception) { showToast(getString(R.string.error_loading_profile)) }
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
        if (name.isEmpty() || country.isEmpty()) { showToast(getString(R.string.validation_complete_fields)); return }

        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.api.updateProfile(mapOf("name" to name, "country" to country))
                if (resp.isSuccessful) {
                    showToast(getString(R.string.success_profile_updated))
                    Prefs(requireContext()).userName = name
                    b.tvName.text = name.uppercase()
                    b.tvCountry.text = country
                } else showToast(getString(R.string.error_saving_profile))
            } catch (e: Exception) { showToast(getString(R.string.error_connection)) }
        }
    }

    private fun uploadPhoto(uri: Uri) {
        val stream = requireContext().contentResolver.openInputStream(uri) ?: return
        val file = File(requireContext().cacheDir, "photo.jpg")
        file.outputStream().use { stream.copyTo(it) }

        val body = MultipartBody.Part.createFormData(
            "photo", file.name, file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        )
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.api.uploadPhoto(body)
                val url = resp.body()?.data?.get("photo_url")
                url?.let {
                    Glide.with(this@ProfileFragment).load(it).circleCrop().into(b.ivAvatar)
                    showToast(getString(R.string.success_photo_updated))
                }
            } catch (e: Exception) { showToast(getString(R.string.error_uploading_photo)) }
        }
    }

    private fun doLogout() {
        lifecycleScope.launch {
            try { RetrofitClient.api.logout() } catch (_: Exception) {}
            Prefs(requireContext()).clear()
            RetrofitClient.clearToken()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finishAffinity()
        }
    }
}
