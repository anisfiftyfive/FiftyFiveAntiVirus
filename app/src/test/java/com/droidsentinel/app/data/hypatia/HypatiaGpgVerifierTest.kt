package com.droidsentinel.app.data.hypatia

import org.bouncycastle.bcpg.HashAlgorithmTags
import org.bouncycastle.bcpg.PublicKeyAlgorithmTags
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters
import org.bouncycastle.openpgp.PGPSignature
import org.bouncycastle.openpgp.PGPSignatureGenerator
import org.bouncycastle.openpgp.operator.bc.BcPGPContentSignerBuilder
import org.bouncycastle.openpgp.operator.bc.BcPGPKeyPair
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.math.BigInteger
import java.security.SecureRandom
import java.util.Date
import java.util.Locale

class HypatiaGpgVerifierTest {
    @Test
    fun verifiesDetachedSha256SignatureWithoutJcaBcProvider() {
        val dir = kotlin.io.path.createTempDirectory("gpg-test").toFile()
        try {
            val generator = RSAKeyPairGenerator()
            generator.init(
                RSAKeyGenerationParameters(BigInteger("10001", 16), SecureRandom(), 2048, 12),
            )
            val pair = BcPGPKeyPair(
                PublicKeyAlgorithmTags.RSA_GENERAL,
                generator.generateKeyPair(),
                Date(),
            )
            val keyId = java.lang.Long.toHexString(pair.publicKey.keyID)
                .uppercase(Locale.US)
                .padStart(16, '0')

            val publicKeyFile = File(dir, "gpg.key")
            publicKeyFile.outputStream().use { pair.publicKey.encode(it) }

            val dataFile = File(dir, "payload.bin")
            dataFile.writeBytes("DroidSentinel-hypatia-fixture".toByteArray())

            val signer = PGPSignatureGenerator(
                BcPGPContentSignerBuilder(pair.publicKey.algorithm, HashAlgorithmTags.SHA256),
            )
            signer.init(PGPSignature.BINARY_DOCUMENT, pair.privateKey)
            signer.update(dataFile.readBytes())
            val signatureFile = File(dir, "payload.bin.sig")
            signatureFile.outputStream().use { signer.generate().encode(it) }

            assertTrue(HypatiaGpgVerifier(keyId).verify(dataFile, signatureFile, publicKeyFile))
        } finally {
            dir.deleteRecursively()
        }
    }
}
