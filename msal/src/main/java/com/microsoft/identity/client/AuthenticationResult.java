//  Copyright (c) Microsoft Corporation.
//  All rights reserved.
//
//  This code is licensed under the MIT License.
//
//  Permission is hereby granted, free of charge, to any person obtaining a copy
//  of this software and associated documentation files(the "Software"), to deal
//  in the Software without restriction, including without limitation the rights
//  to use, copy, modify, merge, publish, distribute, sublicense, and / or sell
//  copies of the Software, and to permit persons to whom the Software is
//  furnished to do so, subject to the following conditions :
//
//  The above copyright notice and this permission notice shall be included in
//  all copies or substantial portions of the Software.
//
//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//  THE SOFTWARE.
package com.microsoft.identity.client;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.microsoft.identity.common.internal.authscheme.TokenAuthenticationScheme;
import com.microsoft.identity.common.internal.cache.ICacheRecord;
import com.microsoft.identity.common.internal.dto.AccessTokenRecord;
import com.microsoft.identity.common.internal.result.ILocalAuthenticationResult;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * MSAL successful authentication result. When auth succeeds, token will be wrapped into the
 * {@link AuthenticationResult} and passed back through the {@link AuthenticationCallback}.
 */
public final class AuthenticationResult implements IAuthenticationResult {

    private final String mTenantId;
    private final AccessTokenRecord mAccessToken;
    private final IAccount mAccount;
    private final String refreshToken;

    AuthenticationResult(@NonNull final ILocalAuthenticationResult localAuthenticationResult) {
        List<ICacheRecord> cacheRecords = localAuthenticationResult.getCacheRecordWithTenantProfileData();
        final ICacheRecord mostRecentlyAuthorized = cacheRecords.get(0);
        mAccessToken = mostRecentlyAuthorized.getAccessToken();
        mTenantId = mostRecentlyAuthorized.getAccount().getRealm();
        mAccount = AccountAdapter.adapt(cacheRecords).get(0);
        refreshToken = localAuthenticationResult.getRefreshToken();
    }

    @Override
    @NonNull
    public String getAccessToken() {
        return mAccessToken.getSecret();
    }

    @NonNull
    @Override
    public String getAuthorizationHeader() {
        final String scheme = mAccessToken.getAccessTokenType();

        return scheme
                + TokenAuthenticationScheme.SCHEME_DELIMITER
                + mAccessToken.getSecret();
    }

    @NonNull
    @Override
    public String getAuthenticationScheme() {
        return mAccessToken.getAccessTokenType();
    }

    @Override
    @NonNull
    public Date getExpiresOn() {
        // TODO how should this work for PoP?
        // Middleware will assume 5 min expiry for PoP tokens
        // Client (MSAL) will not be aware of configured value
        final Date expiresOn;

        expiresOn = new Date(
                TimeUnit.SECONDS.toMillis(
                        Long.parseLong(
                                mAccessToken.getExpiresOn()
                        )
                )
        );

        return expiresOn;
    }

    @Override
    @Nullable
    public String getTenantId() {
        return mTenantId;
    }

    @Nullable
    @Override
    public String getRefreshToken() {
        return refreshToken;
    }

    @Override
    @NonNull
    public IAccount getAccount() {
        return mAccount;
    }

    @Override
    @NonNull
    public String[] getScope() {
        return mAccessToken.getTarget().split("\\s");
    }
}
