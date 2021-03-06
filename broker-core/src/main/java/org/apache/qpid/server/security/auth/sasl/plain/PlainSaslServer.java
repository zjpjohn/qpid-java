/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.qpid.server.security.auth.sasl.plain;

import org.apache.qpid.server.security.auth.AuthenticationResult;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.AuthorizeCallback;
import javax.security.sasl.SaslException;
import javax.security.sasl.SaslServer;
import java.io.IOException;

public class PlainSaslServer implements SaslServer
{
    public static final String MECHANISM = "PLAIN";
    public static final String AUTHENTICATION_RESULT = "authentication-result";

    private CallbackHandler _cbh;

    private String _authorizationId;

    private boolean _complete = false;
    private volatile AuthenticationResult _authenticationResult;

    public PlainSaslServer(CallbackHandler cbh)
    {
        _cbh = cbh;
    }

    public String getMechanismName()
    {
        return MECHANISM;
    }

    public byte[] evaluateResponse(byte[] response) throws SaslException
    {
        int authzidNullPosition = findNullPosition(response, 0);
        if (authzidNullPosition < 0)
        {
            throw new SaslException("Invalid PLAIN encoding, authzid null terminator not found");
        }
        int authcidNullPosition = findNullPosition(response, authzidNullPosition + 1);
        if (authcidNullPosition < 0)
        {
            throw new SaslException("Invalid PLAIN encoding, authcid null terminator not found");
        }

        PlainPasswordCallback passwordCb;
        AuthenticationResultPreservingAuthorizeCallback authzCb;

        try
        {
            // we do not currently support authcid in any meaningful way
            String authzid = new String(response, authzidNullPosition + 1, authcidNullPosition - authzidNullPosition - 1, "utf8");

            // TODO: should not get pwd as a String but as a char array...
            int passwordLen = response.length - authcidNullPosition - 1;
            String pwd = new String(response, authcidNullPosition + 1, passwordLen, "utf8");

            // we do not care about the prompt but it throws if null
            NameCallback nameCb = new NameCallback("prompt", authzid);
            passwordCb = new PlainPasswordCallback("prompt", false, pwd);
            authzCb = new AuthenticationResultPreservingAuthorizeCallback(authzid, authzid);

            Callback[] callbacks = new Callback[]{nameCb, passwordCb, authzCb};
            _cbh.handle(callbacks);
            _authenticationResult = authzCb.getAuthenticationResult();

        }
        catch (IOException e)
        {
            if(e instanceof SaslException)
            {
                throw (SaslException) e;
            }
            throw new SaslException("Error processing data: " + e, e);
        }
        catch (UnsupportedCallbackException e)
        {
            throw new SaslException("Unable to obtain data from callback handler: " + e, e);
        }
        catch (IllegalArgumentException e)
        {
            throw new SaslException("Error processing SASL response: " + e.getMessage(), e);
        }

        if (passwordCb.isAuthenticated())
        {
            _complete = true;
        }

        if (authzCb.isAuthorized() && _complete)
        {
            _authorizationId = authzCb.getAuthenticationID();
            return null;
        }
        else
        {
            throw new SaslException("Authentication failed");
        }
    }



    private int findNullPosition(byte[] response, int startPosition)
    {
        int position = startPosition;
        while (position < response.length)
        {
            if (response[position] == (byte) 0)
            {
                return position;
            }
            position++;
        }
        return -1;
    }

    public boolean isComplete()
    {
        return _complete;
    }

    public String getAuthorizationID()
    {
        return _authorizationId;
    }

    public byte[] unwrap(byte[] incoming, int offset, int len) throws SaslException
    {
        throw new SaslException("Unsupported operation");
    }

    public byte[] wrap(byte[] outgoing, int offset, int len) throws SaslException
    {
        throw new SaslException("Unsupported operation");
    }

    public Object getNegotiatedProperty(String propName)
    {
        if (AUTHENTICATION_RESULT.equals(propName))
        {
            return _authenticationResult;
        }
        return null;
    }

    public void dispose() throws SaslException
    {
        _cbh = null;
    }

    public static class AuthenticationResultPreservingAuthorizeCallback extends AuthorizeCallback
    {
        private volatile AuthenticationResult _authenticationResult;

        public AuthenticationResultPreservingAuthorizeCallback(String authnID, String authzID) {
            super(authnID, authzID);
        }

        public AuthenticationResult getAuthenticationResult()
        {
            return _authenticationResult;
        }

        public void setAuthenticationResult(AuthenticationResult authenticationResult)
        {
            this._authenticationResult = authenticationResult;
        }
    }
}
