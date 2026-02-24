# Jenkins Workspace Cache Issue - Quick Fix

## Problem
Jenkins build masih menggunakan **Jenkinsfile lama** yang tidak memiliki optimasi offline-first build.

## Symptoms
- Build masih menampilkan: `"🔍 Starting Maven build with verbose output..."`
- Build tidak menampilkan: `"Step 1: Try offline mode"`

## Root Cause
Jenkins workspace menggunakan **cached code** yang sudah kedaluwarsa.

---

## Solution 1: Clean Workspace (Recommended)

### Di Jenkins UI:

1. **Buka Job/Pipeline** di Jenkins
2. Klik **"Configure"** di sebelah kiri
3. Scroll ke bagian **"Build Environment"** atau **"Additional Behaviours"**
4. Tambahkan/check:
   - ☑️ **"Delete workspace before build starts"** (atau "Clean before checkout")
5. Klik **"Save"**
6. Jalankan build baru

### Atau via Script Console:

1. Buka: `http://your-jenkins/script`
2. Jalankan:
```groovy
Jenkins.instance.getAllItems(Job.class).each { job ->
  if (job.name.contains('week8')) {
    println "Cleaning workspace for: ${job.name}"
    job.getBuilds().each { it.delete() }
    job.nextBuildNumber = 1
  }
}
```

---

## Solution 2: Manually Clean Workspace

### SSH ke Jenkins Agent:

```bash
# Cari workspace Jenkins
cd /var/jenkins_home/workspace/

# Hapus workspace untuk project ini
rm -rf week8*

# Atau clean semua
rm -rf *
```

### Atau via Jenkins UI:

1. Buka Pipeline run yang failed/timeout
2. Klik **"Restart from beginning"**
3. Pilih ☑️ **"Clean workspace before restart"**

---

## Solution 3: Trigger Fresh Checkout

Tambahkan ini di Jenkins job configuration:

**Pipeline → Build Triggers → Poll SCM**
```
* * * * *
```

Lalu trigger manual dengan:
```bash
curl -X POST http://your-jenkins/job/week8-practice1/build?delay=0sec
```

---

## Verification

Setelah rebuild, pastikan output Jenkins menampilkan:

✅ **GOOD (Using new Jenkinsfile):**
```
📌 Build strategy: Offline-first for fast builds
Step 1: Try offline mode (fast - uses cached dependencies)
```

❌ **BAD (Using old Jenkinsfile):**
```
🔍 Starting Maven build with verbose output...
This will show which dependencies are being downloaded...
mvn clean package ... -X
```

---

## Expected Timeline After Fix

| Build | Mode | Duration | Status |
|-------|------|----------|--------|
| 1st build after clean | Online (downloads) | 15-20 min | ✅ Completes |
| 2nd+ builds | Offline (cached) | 3-5 min | ✅ Fast! |

---

## Troubleshooting

### Jika masih menggunakan kode lama:

1. **Check commit hash in Jenkins output:**
   ```
   Current commit: b88d8dd fix(jenkins): Force clean workspace...
   ```

   Jika tidak menunjukkan commit terbaru, Jenkins belum pull code terbaru.

2. **Check Jenkins Git configuration:**
   - Job → Configure → Pipeline → SCM
   - Pastikan: **Branches to build** = `*/master` (bukan commit hash spesifik)

3. **Check Git plugin di Jenkins:**
   - Manage Jenkins → Plugin Manager
   - Pastikan **Git Plugin** versi terbaru

---

## Contact

If issue persists after trying all solutions:
- Check Jenkins logs: `/var/log/jenkins/jenkins.log`
- Check agent logs: `/var/log/jenkins/agent/slave.log`
- Verify Git connectivity: `git ls-remote https://github.com/mdafaardiansyah/mini-ecommerce-springboot-week8.git`
