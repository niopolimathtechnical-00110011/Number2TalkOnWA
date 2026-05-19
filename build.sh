#!/bin/bash
set -e

echo "🔨 Compilando Number2Talk v2.1..."

# Limpar
rm -rf build *.apk
mkdir -p build/obj build/apk build/gen

# Compilar recursos
echo "📦 Compilando recursos..."
$ANDROID_HOME/build-tools/34.0.0/aapt2 compile --dir app/src/main/res -o build/res.zip

# Linkar recursos
echo "🔗 Linkando recursos..."
$ANDROID_HOME/build-tools/34.0.0/aapt2 link \
  --manifest app/src/main/AndroidManifest.xml \
  -I $ANDROID_HOME/platforms/android-34/android.jar \
  --min-sdk-version 21 \
  --target-sdk-version 34 \
  -o build/Number2Talk-unaligned.apk \
  build/res.zip \
  --java build/gen

# Compilar Java
echo "☕ Compilando código Java..."
javac -d build/obj \
  -classpath $ANDROID_HOME/platforms/android-34/android.jar \
  -source 1.8 -target 1.8 \
  build/gen/com/niotech/number2talk_on_wa/R.java \
  app/src/main/java/com/niotech/number2talk_on_wa/*.java

# Converter para DEX
echo "📱 Convertendo para DEX..."
$ANDROID_HOME/build-tools/34.0.0/d8 \
  --min-api 21 \
  --output build/apk \
  build/obj/com/niotech/number2talk_on_wa/*.class \
  --lib $ANDROID_HOME/platforms/android-34/android.jar

# Empacotar
echo "📦 Empacotando APK..."
cd build/apk && zip -r ../Number2Talk-unaligned.apk classes.dex && cd ../..

# Alinhar
echo "⚙️ Alinhando APK..."
$ANDROID_HOME/build-tools/34.0.0/zipalign -f -p -v 4 \
  build/Number2Talk-unaligned.apk Number2Talk.apk

# Criar keystore se não existir
if [ ! -f release.keystore ]; then
    echo "🔑 Criando keystore..."
    keytool -genkey -v -keystore release.keystore \
      -alias n2t -keyalg RSA -keysize 2048 -validity 20000 \
      -storepass 123456 -keypass 123456 \
      -dname "CN=Enio Alves Borges, OU=Dev, O=NioTech, L=Cuiaba, ST=MT, C=BR"
fi

# Assinar APK
echo "✍️ Assinando APK..."
$ANDROID_HOME/build-tools/34.0.0/apksigner sign \
  --ks release.keystore --ks-pass pass:123456 \
  --v1-signing-enabled true --v2-signing-enabled true \
  --out Number2Talk_v2.1.apk Number2Talk.apk

echo ""
echo "✅ BUILD CONCLUÍDO!"
echo "📱 APK: Number2Talk_v2.1.apk"
ls -lh Number2Talk_v2.1.apk
echo "🔑 SHA256: $(sha256sum Number2Talk_v2.1.apk | cut -d' ' -f1)"
