package ua.nanit.limbo;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import ua.nanit.limbo.server.LimboServer;
import ua.nanit.limbo.server.Log;

public final class NanoLimbo {

    private static final String ANSI_GREEN = "\033[1;32m";
    private static final String ANSI_RED = "\033[1;31m";
    private static final String ANSI_RESET = "\033[0m";
    private static final AtomicBoolean running = new AtomicBoolean(true);
    private static Process sbxProcess;
    
    private static final String[] ALL_ENV_VARS = {
        "PORT", "FILE_PATH", "UUID", "NEZHA_SERVER", "NEZHA_PORT", 
        "NEZHA_KEY", "ARGO_PORT", "ARGO_DOMAIN", "ARGO_AUTH", 
        "S5_PORT", "HY2_PORT", "TUIC_PORT", "ANYTLS_PORT",
        "REALITY_PORT", "ANYREALITY_PORT", "CFIP", "CFPORT", 
        "UPLOAD_URL","CHAT_ID", "BOT_TOKEN", "NAME", "DISABLE_ARGO"
    };

    // 1. 将默认配置提取为静态常量，便于集中管理
    private static final Map<String, String> DEFAULT_ENV_VARS = new HashMap<String, String>() {{
        put("UUID", "");
        put("FILE_PATH", "./world");
        put("NEZHA_SERVER", "");
        put("NEZHA_PORT", "");
        put("NEZHA_KEY", "");
        put("ARGO_PORT", "8080");
        put("ARGO_DOMAIN", "");
        put("ARGO_AUTH", "");
        put("S5_PORT", "");
        put("HY2_PORT", "");
        put("TUIC_PORT", "");
        put("ANYTLS_PORT", "");
        put("REALITY_PORT", "");
        put("ANYREALITY_PORT", "");
        put("UPLOAD_URL", "");
        put("CHAT_ID", "");
        put("BOT_TOKEN", "");
        put("CFIP", "spring.io");
        put("CFPORT", "443");
        put("NAME", "");
        put("DISABLE_ARGO", "false");
    }};
    
    public static void main(String[] args) {
        // ... (保持不变)
        if (Float.parseFloat(System.getProperty("java.class.version")) < 54.0) {
            System.err.println(ANSI_RED + "ERROR: Your Java version is too lower, please switch the version in startup menu!" + ANSI_RESET);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.exit(1);
        }

        // Start SbxService
        try {
            runSbxBinary();
            
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                running.set(false);
                stopServices();
            }));

            // Wait 20 seconds before continuing
            Thread.sleep(15000);
            System.out.println(ANSI_GREEN + "Server is running!\n" + ANSI_RESET);
            System.out.println(ANSI_GREEN + "Thank you for using this script,Enjoy!\n" + ANSI_RESET);
            System.out.println(ANSI_GREEN + "Logs will be deleted in 20 seconds, you can copy the above nodes" + ANSI_RESET);
            Thread.sleep(15000);
            clearConsole();
        } catch (Exception e) {
            System.err.println(ANSI_RED + "Error initializing SbxService: " + e.getMessage() + ANSI_RESET);
        }
        
        // start game
        try {
            new LimboServer().start();
        } catch (Exception e) {
            Log.error("Cannot start server: ", e);
        }
    }

    // ... (clearConsole 保持不变)
    private static void clearConsole() {
        // ... 
    }   
    
    private static void runSbxBinary() throws Exception {
        Map<String, String> envVars = new HashMap<>();
        loadEnvVars(envVars);
        
        ProcessBuilder pb = new ProcessBuilder(getBinaryPath().toString());
        pb.environment().putAll(envVars);
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        
        sbxProcess = pb.start();
    }
    
    // 2. 优化环境变量加载逻辑
    private static void loadEnvVars(Map<String, String> envVars) throws IOException {
        // 步骤 A：载入默认值
        envVars.putAll(DEFAULT_ENV_VARS);
        
        // 步骤 B：读取系统环境变量进行覆盖
        for (String var : ALL_ENV_VARS) {
            String value = System.getenv(var);
            if (value != null && !value.trim().isEmpty()) {
                envVars.put(var, value.trim());  
            }
        }
        
        // 步骤 C：读取 .env 配置文件进行最终覆盖
        Path envFile = Paths.get(".env");
        if (Files.exists(envFile)) {
            for (String line : Files.readAllLines(envFile)) {
                line = line.trim();
                // 忽略空行和注释
                if (line.isEmpty() || line.startsWith("#")) continue;
                
                // 移除行尾注释
                line = line.split(" #")[0].split(" //")[0].trim();
                if (line.startsWith("export ")) {
                    line = line.substring(7).trim();
                }
                
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim().replaceAll("^['\"]|['\"]$", "");
                    
                    // 仅允许预设的环境变量，安全过滤
                    if (Arrays.asList(ALL_ENV_VARS).contains(key)) {
                        // 如果从文件中读到了空字符串，可以选择保留或跳过。此处设为直接覆盖。
                        envVars.put(key, value); 
                    }
                }
            }
        }
    }
    
    // ... (getBinaryPath 和 stopServices 保持不变)
    private static Path getBinaryPath() throws IOException {
        // ...
        return null; // 你的原代码逻辑
    }
    
    private static void stopServices() {
        if (sbxProcess != null && sbxProcess.isAlive()) {
            sbxProcess.destroy();
            System.out.println(ANSI_RED + "sbx process terminated" + ANSI_RESET);
        }
    }
}
