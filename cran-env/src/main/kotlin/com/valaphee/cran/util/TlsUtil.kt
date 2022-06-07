/*
 * Copyright (c) 2022, Valaphee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.valaphee.cran.util

import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.BasicConstraints
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder
import java.io.File
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.Security
import java.security.cert.X509Certificate
import java.util.Calendar
import kotlin.random.asKotlinRandom

/**
 * @author Kevin Ludwig
 */
object TlsUtil {
    private val random = SecureRandom().asKotlinRandom()

    init {
        Security.addProvider(BouncyCastleProvider())
    }

    fun generate() {
        val tlsPath = File("tls")
        if (!tlsPath.exists()) {
            tlsPath.mkdir()

            val root = generateCertificate("CN=Cran")

            val server = generateCertificate(root, "CN=Cran Server")
            JcaPEMWriter(File(tlsPath, "server_key.pem").writer()).use {
                it.writeObject(JcaPKCS8Generator(server.first, null))
                it.flush()
            }
            JcaPEMWriter(File(tlsPath, "server_cer.pem").writer()).use {
                it.writeObject(server.second)
                it.writeObject(root.second)
                it.flush()
            }

            val client = generateCertificate(root, "CN=Cran Client")
            JcaPEMWriter(File(tlsPath, "client_key.pem").writer()).use {
                it.writeObject(JcaPKCS8Generator(client.first, null))
                it.flush()
            }
            JcaPEMWriter(File(tlsPath, "client_cer.pem").writer()).use {
                it.writeObject(client.second)
                it.writeObject(root.second)
                it.flush()
            }
        }
    }

    fun generateCertificate(subject: String): Pair<PrivateKey, X509Certificate> {
        val keyPair = KeyPairGenerator.getInstance("RSA", "BC").apply { initialize(2048) }.generateKeyPair()
        return keyPair.private to JcaX509CertificateConverter().setProvider("BC").getCertificate(JcaX509v3CertificateBuilder(X500Name(subject), BigInteger(random.nextBytes(8)), Calendar.getInstance().apply { add(Calendar.DATE, -1) }.time, Calendar.getInstance().apply { add(Calendar.YEAR, 5) }.time, X500Name(subject), keyPair.public).apply {
            addExtension(Extension.basicConstraints, true, BasicConstraints(true))
            addExtension(Extension.subjectKeyIdentifier, false, JcaX509ExtensionUtils().createSubjectKeyIdentifier(keyPair.public))
        }.build(JcaContentSignerBuilder("SHA256withRSA").setProvider("BC").build(keyPair.private)))
    }

    fun generateCertificate(parent: Pair<PrivateKey, X509Certificate>, subject: String): Pair<PrivateKey, X509Certificate> {
        val keyPair = KeyPairGenerator.getInstance("RSA", "BC").apply { initialize(2048) }.generateKeyPair()
        val rootSigner = JcaContentSignerBuilder("SHA256withRSA").setProvider("BC").build(parent.first)
        val csr = JcaPKCS10CertificationRequestBuilder(X500Name(subject), keyPair.public).build(rootSigner)
        return keyPair.private to JcaX509CertificateConverter().setProvider("BC").getCertificate(X509v3CertificateBuilder(JcaX509CertificateHolder(parent.second).subject, BigInteger(random.nextBytes(8)), Calendar.getInstance().apply { add(Calendar.DATE, -1) }.time, Calendar.getInstance().apply { add(Calendar.YEAR, 1) }.time, csr.subject, csr.subjectPublicKeyInfo).apply {
            addExtension(Extension.basicConstraints, true, BasicConstraints(false))
            addExtension(Extension.authorityKeyIdentifier, false, JcaX509ExtensionUtils().createAuthorityKeyIdentifier(parent.second))
            addExtension(Extension.subjectKeyIdentifier, false, JcaX509ExtensionUtils().createSubjectKeyIdentifier(csr.subjectPublicKeyInfo))
        }.build(rootSigner))
    }
}
