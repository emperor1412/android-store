package com.soomla.store.data;

import android.content.SharedPreferences;
import android.util.Base64;

import com.soomla.SoomlaConfig;
import com.soomla.store.SoomlaApp;
import com.soomla.SoomlaUtils;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.util.Map;
import java.util.Set;

public class ObscuredSharedPreferences implements SharedPreferences {
    protected static final String UTF8 = "utf-8";


    protected SharedPreferences delegate;

    public ObscuredSharedPreferences(SharedPreferences delegate) {
        this.delegate = delegate;
    }

    public class Editor implements SharedPreferences.Editor {
        protected SharedPreferences.Editor delegate;

        public Editor() {
            this.delegate = ObscuredSharedPreferences.this.delegate.edit();
        }

        @Override
        public Editor putBoolean(String key, boolean value) {
            delegate.putString(key, encrypt(Boolean.toString(value)));
            return this;
        }

        @Override
        public Editor putFloat(String key, float value) {
            delegate.putString(key, encrypt(Float.toString(value)));
            return this;
        }

        @Override
        public Editor putInt(String key, int value) {
            delegate.putString(key, encrypt(Integer.toString(value)));
            return this;
        }

        @Override
        public Editor putLong(String key, long value) {
            delegate.putString(key, encrypt(Long.toString(value)));
            return this;
        }

        @Override
        public Editor putString(String key, String value) {
            delegate.putString(key, encrypt(value));
            return this;
        }

        @Override
        public SharedPreferences.Editor putStringSet(String s, Set<String> strings) {
            throw new UnsupportedOperationException("putStringSet is not supported in ObscuredSharedPreferences");
        }

        @Override
        public void apply() {
            delegate.apply();
        }

        @Override
        public Editor clear() {
            delegate.clear();
            return this;
        }

        @Override
        public boolean commit() {
            return delegate.commit();
        }

        @Override
        public Editor remove(String s) {
            delegate.remove(s);
            return this;
        }
    }

    public Editor edit() {
        return new Editor();
    }


    @Override
    public Map<String, ?> getAll() {
        throw new UnsupportedOperationException(); // left as an exercise to the reader
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        final String v = delegate.getString(key, null);
        return v!=null ? Boolean.parseBoolean(decrypt(v)) : defValue;
    }

    @Override
    public float getFloat(String key, float defValue) {
        final String v = delegate.getString(key, null);
        return v!=null ? Float.parseFloat(decrypt(v)) : defValue;
    }

    @Override
    public int getInt(String key, int defValue) {
        final String v = delegate.getString(key, null);
        return v!=null ? Integer.parseInt(decrypt(v)) : defValue;
    }

    @Override
    public long getLong(String key, long defValue) {
        final String v = delegate.getString(key, null);
        return v!=null ? Long.parseLong(decrypt(v)) : defValue;
    }

    @Override
    public String getString(String key, String defValue) {
        final String v = delegate.getString(key, null);
        return v != null ? decrypt(v) : defValue;
    }

    @Override
    public Set<String> getStringSet(String s, Set<String> strings) {
        throw new UnsupportedOperationException("getStringSet is not supported in ObscuredSharedPreferences");
    }

    @Override
    public boolean contains(String s) {
        return delegate.contains(s);
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        delegate.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        delegate.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }




    protected String encrypt( String value ) {

        try {
            final byte[] bytes = value!=null ? value.getBytes(UTF8) : new byte[0];
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            SecretKey key = keyFactory.generateSecret(new PBEKeySpec((SoomlaConfig.SOOM_SEC + SoomlaApp.getAppContext().getPackageName() + SoomlaUtils.deviceId()).toCharArray()));
            Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
            pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(SoomlaUtils.deviceId().getBytes(UTF8), 20));
            return new String(Base64.encode(pbeCipher.doFinal(bytes), Base64.NO_WRAP),UTF8);

        } catch( Exception e ) {
            throw new RuntimeException(e);
        }

    }

    protected String decrypt(String value){
        try {
            final byte[] bytes = value!=null ? Base64.decode(value,Base64.DEFAULT) : new byte[0];
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            SecretKey key = keyFactory.generateSecret(new PBEKeySpec((SoomlaConfig.SOOM_SEC + SoomlaApp.getAppContext().getPackageName() + SoomlaUtils.deviceId()).toCharArray()));
            Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
            pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(SoomlaUtils.deviceId().getBytes(UTF8), 20));
            return new String(pbeCipher.doFinal(bytes),UTF8);

        } catch( Exception e) {
            throw new RuntimeException(e);
        }
    }

}
