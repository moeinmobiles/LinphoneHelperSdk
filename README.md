# LinphoneHelperSdk

## Import


```groovy
allprojects {
    repositories {
        //
        maven {url 'https://jitpack.io'}
        maven {url"https://linphone.org/maven_repository"}
    }
}


dependencies {
 ...
    implementation 'com.github.moeinmobiles:LinphoneHelperSdk:0.3'
}

```

## Usage
### 1. Start Linphone in the application class

```java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LinphoneImpl.startLinphone(this);
    }
}

```

### 2. Register your account

```java
LinphoneImpl.setAccount(username, password, domain);
```

### 3. If you want to monitor the account registration process you can implement the Registration callback in your activity 

```java
MainActivity extends AppCompatActivity implements RegistrationCallback {

///

@Override
public void registrationNone() {
}

@Override
public void registrationProgress() {
}

@Override
public void registrationOk() {
}

@Override
public void registrationCleared() {
}

@Override
public void registrationFailed() {
}


} 

```

### 4. start the call

```java
LinphoneImpl.callTo(contactUserName, context);
```

   
   
