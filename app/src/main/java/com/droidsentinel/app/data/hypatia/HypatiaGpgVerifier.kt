package com.droidsentinel.app.data.hypatia

import org.bouncycastle.openpgp.PGPObjectFactory
import org.bouncycastle.openpgp.PGPPublicKey
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection
import org.bouncycastle.openpgp.PGPSignature
import org.bouncycastle.openpgp.PGPSignatureList
import org.bouncycastle.openpgp.PGPUtil
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator
import org.bouncycastle.openpgp.operator.bc.BcPGPContentVerifierBuilderProvider
import java.io.File
import java.io.FileInputStream
import java.util.Locale

/**
 * Verifies Hypatia detached GPG signatures using BouncyCastle's lightweight API.
 *
 * Android ships a stripped "BC" JCA provider that does not implement SHA-256.
 * JcaPGPContentVerifierBuilderProvider.setProvider("BC") therefore fails with
 * NoSuchAlgorithmException. The `operator.bc` APIs do not go through JCA.
 */
class HypatiaGpgVerifier(
    private val expectedKeyId: String = HypatiaConstants.SIGNING_KEY_ID,
) {
    fun verify(signedFile: File, signatureFile: File, publicKeyFile: File): Boolean {
        FileInputStream(signatureFile).use { signatureStream ->
            val factory = PGPObjectFactory(
                PGPUtil.getDecoderStream(signatureStream),
                BcKeyFingerprintCalculator(),
            )
            val obj = factory.nextObject() as? PGPSignatureList ?: return false
            if (obj.size() < 1) return false
            val signature: PGPSignature = obj.get(0)
            val publicKey = readPublicKey(publicKeyFile) ?: return false
            signature.init(BcPGPContentVerifierBuilderProvider(), publicKey)
            FileInputStream(signedFile).use { data ->
                val buffer = ByteArray(64 * 1024)
                while (true) {
                    val read = data.read(buffer)
                    if (read <= 0) {
                        if (read == -1) break
                        continue
                    }
                    signature.update(buffer, 0, read)
                }
            }
            return signature.verify()
        }
    }

    private fun readPublicKey(file: File): PGPPublicKey? {
        FileInputStream(file).use { input ->
            val rings = PGPPublicKeyRingCollection(
                PGPUtil.getDecoderStream(input),
                BcKeyFingerprintCalculator(),
            )
            val wanted = expectedKeyId.uppercase(Locale.US)
            for (ring in rings.keyRings) {
                for (key in ring.publicKeys) {
                    val id = java.lang.Long.toHexString(key.keyID).uppercase(Locale.US).padStart(16, '0')
                    if (id.endsWith(wanted)) return key
                }
            }
            return null
        }
    }
}
