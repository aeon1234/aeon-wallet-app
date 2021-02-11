#include <cinttypes>
#include "wallet2_api.h"
#include <android/log.h>

#include <jni.h>

jfieldID getHandleField(JNIEnv *env, jobject obj, const char *fieldName = "handle") {
    jclass c = env->GetObjectClass(obj);
    return env->GetFieldID(c, fieldName, "J"); // of type long
}

template<typename T>
T *getHandle(JNIEnv *env, jobject obj, const char *fieldName = "handle") {
    jlong handle = env->GetLongField(obj, getHandleField(env, obj, fieldName));
    return reinterpret_cast<T *>(handle);
}
#ifdef __cplusplus
extern "C"
{
#endif
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved) {
    JNIEnv *jenv;
    if (jvm->GetEnv(reinterpret_cast<void **>(&jenv), JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }
    return JNI_VERSION_1_6;
}
JNIEXPORT jint JNICALL
Java_com_aeon_wallet_app_models_TransactionPending_getStatusJNI(
        JNIEnv *env,
        jobject instance
) {
    auto *pendingTransaction = getHandle<Bitmonero::PendingTransaction>(env, instance);
    return pendingTransaction->status();
}

JNIEXPORT jlong JNICALL
Java_com_aeon_wallet_app_models_TransactionPending_getFeeJNI(
        JNIEnv *env,
        jobject instance
) {
    auto *pendingTransaction = getHandle<Bitmonero::PendingTransaction>(env, instance);
    return pendingTransaction->fee();
}
JNIEXPORT jlong JNICALL
Java_com_aeon_wallet_app_models_TransactionPending_getAmountJNI(
        JNIEnv *env,
        jobject instance
) {
    auto *pendingTransaction = getHandle<Bitmonero::PendingTransaction>(env, instance);
    return pendingTransaction->amount();
}
JNIEXPORT jlong JNICALL
Java_com_aeon_wallet_app_models_TransactionPending_getDustJNI(
        JNIEnv *env,
        jobject instance
) {
    auto *pendingTransaction = getHandle<Bitmonero::PendingTransaction>(env, instance);
    return pendingTransaction->dust();
}
JNIEXPORT jboolean JNICALL
Java_com_aeon_wallet_app_models_TransactionPending_commitJNI(JNIEnv *env, jobject instance) {
    auto *pendingTransaction = getHandle<Bitmonero::PendingTransaction>(env, instance);
    bool success = pendingTransaction->commit();
    return static_cast<jboolean>(success);
}

JNIEXPORT jint JNICALL
Java_com_aeon_wallet_app_models_Transaction_getDirectionJNI(
        JNIEnv *env,
        jobject instance
) {
    auto *transactionInfo = getHandle<Bitmonero::TransactionInfo>(env, instance);
    if (transactionInfo == nullptr) {
        return 2;
    } else {
        return transactionInfo->direction();
    }
}
JNIEXPORT jboolean JNICALL
Java_com_aeon_wallet_app_models_Transaction_isPendingJNI(
        JNIEnv *env,
        jobject instance
) {
    auto *transactionInfo = getHandle<Bitmonero::TransactionInfo>(env, instance);
    return transactionInfo->isPending();
}
JNIEXPORT jboolean JNICALL
Java_com_aeon_wallet_app_models_Transaction_isFailedJNI(
        JNIEnv *env,
        jobject instance
) {
    auto *transactionInfo = getHandle<Bitmonero::TransactionInfo>(env, instance);
    return transactionInfo->isFailed();
}
JNIEXPORT jlong JNICALL
Java_com_aeon_wallet_app_models_Transaction_getAmountJNI(
        JNIEnv *env,
        jobject instance
) {
    auto *transactionInfo = getHandle<Bitmonero::TransactionInfo>(env, instance);
    return transactionInfo->amount();
}
JNIEXPORT jlong JNICALL
Java_com_aeon_wallet_app_models_Transaction_getFeeJNI(
        JNIEnv *env,
        jobject instance
) {
    auto *transactionInfo = getHandle<Bitmonero::TransactionInfo>(env, instance);
    return transactionInfo->fee();
}
JNIEXPORT jlong JNICALL
Java_com_aeon_wallet_app_models_Transaction_getHeightJNI(
        JNIEnv *env,
        jobject instance
) {
    auto *transactionInfo = getHandle<Bitmonero::TransactionInfo>(env, instance);
    return transactionInfo->blockHeight();
}
JNIEXPORT jlong JNICALL
Java_com_aeon_wallet_app_models_Transaction_getConfirmationsJNI(
        JNIEnv *env,
        jobject instance
) {
    auto *transactionInfo = getHandle<Bitmonero::TransactionInfo>(env, instance);
    return transactionInfo->confirmations();
}
JNIEXPORT jlong JNICALL
Java_com_aeon_wallet_app_models_Transaction_getUnlockTimeJNI(
        JNIEnv *env,
        jobject instance
) {
    auto *transactionInfo = getHandle<Bitmonero::TransactionInfo>(env, instance);
    return transactionInfo->unlockTime();
}
JNIEXPORT jstring JNICALL
Java_com_aeon_wallet_app_models_Transaction_getHashJNI(
        JNIEnv *env,
        jobject instance
) {
    auto *transactionInfo = getHandle<Bitmonero::TransactionInfo>(env, instance);
    return env->NewStringUTF(transactionInfo->hash().c_str());
}
JNIEXPORT jstring JNICALL
Java_com_aeon_wallet_app_models_Transaction_getPaymentIdJNI(
        JNIEnv *env,
        jobject instance
) {
    auto *transactionInfo = getHandle<Bitmonero::TransactionInfo>(env,  instance);
    return env->NewStringUTF(transactionInfo->paymentId().c_str());
}
JNIEXPORT jlong JNICALL
Java_com_aeon_wallet_app_models_Transaction_getTimestampJNI(
        JNIEnv *env,
        jobject instance
) {
    auto *transactionInfo = getHandle<Bitmonero::TransactionInfo>(env,  instance);
    return transactionInfo->timestamp();
}

JNIEXPORT jint JNICALL
Java_com_aeon_wallet_app_models_TransactionHistory_getCountJNI(
        JNIEnv *env,
        jobject instance
) {
    auto *transactionHistory = getHandle<Bitmonero::TransactionHistory>(env, instance);
    return transactionHistory->count();
}
JNIEXPORT jlong JNICALL
Java_com_aeon_wallet_app_models_TransactionHistory_getTransactionJNI(
        JNIEnv *env,
        jobject instance,
        jint index
) {
    auto *transactionHistory = getHandle<Bitmonero::TransactionHistory>(env, instance);
    Bitmonero::TransactionInfo *transactionInfo = transactionHistory->transaction(index);
    return reinterpret_cast<jlong>(transactionInfo);
}
JNIEXPORT void JNICALL
Java_com_aeon_wallet_app_models_TransactionHistory_refreshJNI(
        JNIEnv *env,
        jobject instance
) {
    auto *transactionHistory = getHandle<Bitmonero::TransactionHistory>(env, instance);
    transactionHistory->refresh();
}
JNIEXPORT jlong JNICALL
Java_com_aeon_wallet_app_models_Wallet_createJNI(
        JNIEnv *env,
        jobject instance,
        jstring path,
        jstring password,
        jstring language
    ) {
    const char *_path = env->GetStringUTFChars(path, nullptr);
    const char *_password = env->GetStringUTFChars(password, nullptr);
    const char *_language = env->GetStringUTFChars(language, nullptr);

    auto *wallet =
            Bitmonero::WalletManagerFactory::getWalletManager()->createWallet(
                    std::string(_path),
                    std::string(_password),
                    std::string(_language)
            );

    env->ReleaseStringUTFChars(path, _path);
    env->ReleaseStringUTFChars(password, _password);
    env->ReleaseStringUTFChars(language, _language);
    return reinterpret_cast<jlong>(wallet);
}

JNIEXPORT jlong JNICALL
Java_com_aeon_wallet_app_models_Wallet_createFromSeedJNI(
        JNIEnv *env,
        jobject instance,
        jstring path,
        jstring password,
        jstring seed,
        jlong restoreHeight
) {
    const char *_path = env->GetStringUTFChars(path, nullptr);
    const char *_password = env->GetStringUTFChars(password, nullptr);
    const char *_seed = env->GetStringUTFChars(seed, nullptr);
    auto _networkType = static_cast<Bitmonero::NetworkType>(0);
    auto *wallet =
            Bitmonero::WalletManagerFactory::getWalletManager()->recoveryWallet(
                    std::string(_path),
                    std::string(_password),
                    std::string(_seed),
                    _networkType,
                    restoreHeight
            );
    env->ReleaseStringUTFChars(path, _path);
    env->ReleaseStringUTFChars(password, _password);
    env->ReleaseStringUTFChars(seed, _seed);
    return reinterpret_cast<jlong>(wallet);
}

JNIEXPORT jlong JNICALL
Java_com_aeon_wallet_app_models_Wallet_createFromKeysJNI(
        JNIEnv *env,
        jobject instance,
        jstring path,
        jstring password,
        jstring address,
        jstring view,
        jstring spend,
        jlong restoreHeight
) {
    const char *_path = env->GetStringUTFChars(path, nullptr);
    const char *_password = env->GetStringUTFChars(password, nullptr);
    const char *_address = env->GetStringUTFChars(address, nullptr);
    const char *_view = env->GetStringUTFChars(view, nullptr);
    const char *_spend = env->GetStringUTFChars(spend, nullptr);
    auto _networkType = static_cast<Bitmonero::NetworkType>(0);
    auto *wallet =
            Bitmonero::WalletManagerFactory::getWalletManager()->createWalletFromKeys(
                    std::string(_path),
                    std::string(_password),
                    _networkType,
                    restoreHeight,
                    std::string(_address),
                    std::string(_view),
                    std::string(_spend)
            );
    env->ReleaseStringUTFChars(path, _path);
    env->ReleaseStringUTFChars(password, _password);
    env->ReleaseStringUTFChars(address, _address);
    env->ReleaseStringUTFChars(view, _view);
    env->ReleaseStringUTFChars(spend, _spend);
    return reinterpret_cast<jlong>(wallet);
}

JNIEXPORT jlong JNICALL
Java_com_aeon_wallet_app_models_Wallet_openWalletJNI(
        JNIEnv *env,
        jobject instance,
        jstring path,
        jstring password
    ) {
    const char *_path = env->GetStringUTFChars(path, nullptr);
    const char *_password = env->GetStringUTFChars(password, nullptr);
    auto _networkType = static_cast<Bitmonero::NetworkType>(0);
    auto *wallet =
        Bitmonero::WalletManagerFactory::getWalletManager()->openWallet(
            std::string(_path),
            std::string(_password),
            _networkType
    );
    env->ReleaseStringUTFChars(path, _path);
    env->ReleaseStringUTFChars(password, _password);
    return reinterpret_cast<jlong>(wallet);
}

JNIEXPORT jboolean JNICALL
Java_com_aeon_wallet_app_models_Wallet_isExistsJNI(
        JNIEnv *env,
        jobject instance,
        jstring path
    ) {
    const char *_path = env->GetStringUTFChars(path, nullptr);
    bool exists =
            Bitmonero::WalletManagerFactory::getWalletManager()->walletExists(std::string(_path));
    env->ReleaseStringUTFChars(path, _path);
    return static_cast<jboolean>(exists);
}

JNIEXPORT void JNICALL
Java_com_aeon_wallet_app_models_Wallet_setDaemonAddressJNI(
        JNIEnv *env,
        jobject instance,
        jstring address
) {
    const char *_address = env->GetStringUTFChars(address, nullptr);
    Bitmonero::WalletManagerFactory::getWalletManager()->setDaemonAddress(std::string(_address));
    env->ReleaseStringUTFChars(address, _address);
}

JNIEXPORT jint JNICALL
Java_com_aeon_wallet_app_models_Wallet_getDaemonVersionJNI(
        JNIEnv *env,
        jobject instance
    ) {
    uint32_t version;
    bool isConnected =
            Bitmonero::WalletManagerFactory::getWalletManager()->connected(&version);
    if (!isConnected) version = 0;
    return version;
}

JNIEXPORT jint JNICALL
Java_com_aeon_wallet_app_models_Wallet_getStatusJNI(
        JNIEnv *env,
        jobject instance
    ) {
    auto *wallet = getHandle<Bitmonero::Wallet>(env, instance);
    return wallet->status();
}

JNIEXPORT jint JNICALL
Java_com_aeon_wallet_app_models_Wallet_getConnectionStatusJNI(
        JNIEnv *env,
        jobject instance
) {
    auto *wallet = getHandle<Bitmonero::Wallet>(env, instance);
    return wallet->connected();
}


JNIEXPORT jstring JNICALL
Java_com_aeon_wallet_app_models_Wallet_getPathJNI(
        JNIEnv *env,
        jobject instance
    ) {
    auto *wallet = getHandle<Bitmonero::Wallet>(env, instance);
    return env->NewStringUTF(wallet->path().c_str());
}

JNIEXPORT jboolean JNICALL
Java_com_aeon_wallet_app_models_Wallet_initJNI(
        JNIEnv *env,
        jobject instance,
        jstring daemon_address,
        jlong upper_transaction_size_limit,
        jstring daemon_username,
        jstring daemon_password
    ) {
    const char *_daemon_address = env->GetStringUTFChars(daemon_address, nullptr);
    const char *_daemon_username = env->GetStringUTFChars(daemon_username, nullptr);
    const char *_daemon_password = env->GetStringUTFChars(daemon_password, nullptr);
    auto *wallet = getHandle<Bitmonero::Wallet>(env, instance);
    bool status = wallet->init(_daemon_address,
        (uint64_t) upper_transaction_size_limit,
        _daemon_username,
        _daemon_password
    );
    env->ReleaseStringUTFChars(daemon_address, _daemon_address);
    env->ReleaseStringUTFChars(daemon_username, _daemon_username);
    env->ReleaseStringUTFChars(daemon_password, _daemon_password);
    return static_cast<jboolean>(status);
}

JNIEXPORT jstring JNICALL
Java_com_aeon_wallet_app_models_Wallet_getSeedJNI(
        JNIEnv *env,
        jobject instance
) {
    auto *wallet = getHandle<Bitmonero::Wallet>(env, instance);
    return env->NewStringUTF(wallet->seed().c_str());
}
JNIEXPORT jstring JNICALL
Java_com_aeon_wallet_app_models_Wallet_getSecretViewKeyJNI(
        JNIEnv *env,
        jobject instance
) {
    auto *wallet = getHandle<Bitmonero::Wallet>(env, instance);
    return env->NewStringUTF(wallet->secretViewKey().c_str());
}
JNIEXPORT jstring JNICALL
Java_com_aeon_wallet_app_models_Wallet_getPublicViewKeyJNI(
        JNIEnv *env,
        jobject instance
) {
    auto *wallet = getHandle<Bitmonero::Wallet>(env, instance);
    return env->NewStringUTF(wallet->publicViewKey().c_str());
}

JNIEXPORT jstring JNICALL
Java_com_aeon_wallet_app_models_Wallet_getSecretSpendKeyJNI(
        JNIEnv *env,
        jobject instance
) {
    auto *wallet = getHandle<Bitmonero::Wallet>(env, instance);
    return env->NewStringUTF(wallet->secretSpendKey().c_str());
}
JNIEXPORT jstring JNICALL
Java_com_aeon_wallet_app_models_Wallet_getPublicSpendKeyJNI(
        JNIEnv *env,
        jobject instance
) {
    auto *wallet = getHandle<Bitmonero::Wallet>(env, instance);
    return env->NewStringUTF(wallet->publicSpendKey().c_str());
}
JNIEXPORT jstring JNICALL
Java_com_aeon_wallet_app_models_Wallet_getAddressJNI(
        JNIEnv *env,
        jobject instance,
        jint accountIndex,
        jint addressIndex
) {
    auto *wallet = getHandle<Bitmonero::Wallet>(env, instance);
    return env->NewStringUTF(
            wallet->address((uint32_t) accountIndex, (uint32_t) addressIndex).c_str());
}
JNIEXPORT jlong JNICALL
Java_com_aeon_wallet_app_models_Wallet_getBalanceJNI(
        JNIEnv *env,
        jobject instance,
        jint accountIndex
) {
    auto *wallet = getHandle<Bitmonero::Wallet>(env, instance);
    return wallet->balance((uint32_t) accountIndex);
}

JNIEXPORT jlong JNICALL
Java_com_aeon_wallet_app_models_Wallet_getUnlockedBalanceJNI(
        JNIEnv *env,
        jobject instance,
        jint accountIndex
) {
    auto *wallet = getHandle<Bitmonero::Wallet>(env, instance);
    return wallet->unlockedBalance((uint32_t) accountIndex);
}

JNIEXPORT jlong JNICALL
Java_com_aeon_wallet_app_models_Wallet_getDaemonBlockChainHeightJNI(
        JNIEnv *env,
        jobject instance
) {
    auto *wallet = getHandle<Bitmonero::Wallet>(env, instance);
    return wallet->daemonBlockChainHeight();
}

JNIEXPORT jlong JNICALL
Java_com_aeon_wallet_app_models_Wallet_getDaemonBlockChainTargetHeightJNI(
        JNIEnv *env,
        jobject instance
) {
    auto *wallet = getHandle<Bitmonero::Wallet>(env, instance);
    return wallet->daemonBlockChainTargetHeight();
}

JNIEXPORT jlong JNICALL
Java_com_aeon_wallet_app_models_Wallet_getBlockChainHeightJNI(
        JNIEnv *env,
        jobject instance
) {
    auto *wallet = getHandle<Bitmonero::Wallet>(env, instance);
    return wallet->blockChainHeight();
}

JNIEXPORT jlong JNICALL
Java_com_aeon_wallet_app_models_Wallet_getBlockChainHeightEstimateJNI(
        JNIEnv *env,
        jobject instance
) {
    auto *wallet = getHandle<Bitmonero::Wallet>(env, instance);
    return wallet->approximateBlockChainHeight();
}

JNIEXPORT jlong JNICALL
Java_com_aeon_wallet_app_models_Wallet_getRefreshFromBlockHeightJNI(
        JNIEnv *env,
        jobject instance
) {
    auto *wallet = getHandle<Bitmonero::Wallet>(env, instance);
    return wallet->getRefreshFromBlockHeight();
}
JNIEXPORT jboolean JNICALL
Java_com_aeon_wallet_app_models_Wallet_isSynchronizedJNI(
        JNIEnv *env,
        jobject instance
    ) {
    auto *wallet = getHandle<Bitmonero::Wallet>(env, instance);
    return static_cast<jboolean>(wallet->synchronized());
}

JNIEXPORT void JNICALL
Java_com_aeon_wallet_app_models_Wallet_startRefreshJNI(
        JNIEnv *env,
        jobject instance
) {
    auto *wallet = getHandle<Bitmonero::Wallet>(env, instance);
    wallet->startRefresh();
}
JNIEXPORT void JNICALL
Java_com_aeon_wallet_app_models_Wallet_pauseRefreshJNI(
        JNIEnv *env,
        jobject instance
) {
    auto *wallet = getHandle<Bitmonero::Wallet>(env, instance);
    wallet->pauseRefresh();
}
JNIEXPORT void JNICALL
Java_com_aeon_wallet_app_models_Wallet_setRefreshHeightJNI(
        JNIEnv *env,
        jobject instance,
        jlong height
) {
    auto *wallet = getHandle<Bitmonero::Wallet>(env, instance);
    wallet->setRefreshFromBlockHeight(height);
}

JNIEXPORT jlong JNICALL
Java_com_aeon_wallet_app_models_Wallet_getRefreshHeightJNI(
        JNIEnv *env,
        jobject instance
) {
    auto *wallet = getHandle<Bitmonero::Wallet>(env, instance);
    return wallet->getRefreshFromBlockHeight();
}

JNIEXPORT void JNICALL
Java_com_aeon_wallet_app_models_Wallet_storeJNI(
        JNIEnv *env,
        jobject instance,
        jstring path
) {
    const char *_path = env->GetStringUTFChars(path, nullptr);
    auto *wallet = getHandle<Bitmonero::Wallet>(env, instance);
    wallet->store(_path);
    env->ReleaseStringUTFChars(path, _path);
}


JNIEXPORT jlong JNICALL
Java_com_aeon_wallet_app_models_Wallet_getTransactionHistoryJNI(
        JNIEnv *env,
        jobject instance
    ) {
    auto *wallet = getHandle<Bitmonero::Wallet>(env, instance);
    return reinterpret_cast<jlong>(wallet->history());
}

JNIEXPORT void JNICALL
Java_com_aeon_wallet_app_models_Wallet_disposeTransactionJNI(JNIEnv *env, jobject instance,
                                                                   jobject pendingTransaction) {
    auto *wallet = getHandle<Bitmonero::Wallet>(env, instance);
    auto *_pendingTransaction =
            getHandle<Bitmonero::PendingTransaction>(env, pendingTransaction);
    wallet->disposeTransaction(_pendingTransaction);
}
JNIEXPORT jlong JNICALL
Java_com_aeon_wallet_app_models_Wallet_createTransactionJNI(
        JNIEnv *env,
        jobject instance,
        jstring dst_address,
        jstring payment_id,
        jlong amount,
        jint ring_size,
        jint priority
) {
    const char *_dst_address = env->GetStringUTFChars(dst_address, nullptr);
    const char *_payment_id = env->GetStringUTFChars(payment_id, nullptr);
    auto _priority = static_cast<Bitmonero::PendingTransaction::Priority>(priority);
    auto *wallet = getHandle<Bitmonero::Wallet>(env, instance);
    Bitmonero::PendingTransaction *pendingTransaction = wallet->createTransaction(
            _dst_address,
            _payment_id,
            amount,
            ring_size,
            _priority
    );
    env->ReleaseStringUTFChars(dst_address, _dst_address);
    env->ReleaseStringUTFChars(payment_id, _payment_id);
    return reinterpret_cast<jlong>(pendingTransaction);
}
JNIEXPORT jlong JNICALL
Java_com_aeon_wallet_app_models_Wallet_createSweepAllJNI(
        JNIEnv *env,
        jobject instance,
        jstring dst_address,
        jstring payment_id,
        jint ring_size,
        jint priority
) {
    const char *_dst_address = env->GetStringUTFChars(dst_address, nullptr);
    const char *_payment_id = env->GetStringUTFChars(payment_id, nullptr);
    auto _priority = static_cast<Bitmonero::PendingTransaction::Priority>(priority);
    auto *wallet = getHandle<Bitmonero::Wallet>(env, instance);
    Bitmonero::optional<uint64_t> empty;
    Bitmonero::PendingTransaction *pendingTransaction = wallet->createTransaction(
            _dst_address,
            _payment_id,
            empty,
            ring_size,
            _priority
    );
    env->ReleaseStringUTFChars(dst_address, _dst_address);
    env->ReleaseStringUTFChars(payment_id, _payment_id);
    return reinterpret_cast<jlong>(pendingTransaction);
}

#ifdef __cplusplus
}
#endif