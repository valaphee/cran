mkdir build\generated\source\proto\main\java\
for /R "src\main\proto" %%f in (*.proto) do ..\tools\protoc-3.20.0-win64\bin\protoc.exe -I=%cd%\src\main\proto --java_out=build\generated\source\proto\main\java\ --plugin=protoc-gen-grpc-java=..\tools\protoc-gen-grpc-java-1.46.0-windows-x86_64.exe --grpc-java_out=build\generated\source\proto\main\java\ "%%f"
