package com.example.akleg;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.*;

public class AkLegSenateScraper {

    public static class Member {
        public String Name = "";
        public String Title = "";
        public String Position = "";
        public String Party = "";
        public String Address = "";
        public String Phone = "";
        public String Email = "";
        public String URL = "";
    }

    public static void main(String[] args) throws Exception {
        // Setup ChromeDriver (auto-managed)
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        // Uncomment to run headless:
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        try {
            String baseUrl = "https://akleg.gov/senate.php";
            driver.get(baseUrl);

            // Collect member detail hrefs (unique)
            List<WebElement> anchors = driver.findElements(By.cssSelector("a[href*='/basis/Member/Detail/']"));
            Set<String> hrefs = new LinkedHashSet<>();
            for (WebElement a : anchors) {
                String href = a.getAttribute("href");
                if (href != null && href.contains("/basis/Member/Detail/")) {
                    hrefs.add(href);
                }
            }

            System.out.println("Found " + hrefs.size() + " member links.");

            List<Member> members = new ArrayList<>();

            for (String href : hrefs) {
                try {
                    driver.get(href);
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

                    String bodyText = driver.findElement(By.tagName("body")).getText();

                    // Name
                    String name = "";
                    try {
                        WebElement h1 = driver.findElement(By.xpath("//h1"));
                        name = h1.getText().trim();
                    } catch (Exception ex) {
                        try {
                            WebElement h2 = driver.findElement(By.xpath("//h2"));
                            name = h2.getText().trim();
                        } catch (Exception ex2) {
                            // fallback: look for first strong or bold near top
                            try {
                                WebElement strong = driver.findElement(By.xpath("//strong"));
                                name = strong.getText().trim();
                            } catch (Exception ignored) {}
                        }
                    }

                    // Title
                    String title = "";
                    try {
                        List<WebElement> roleVec = driver.findElements(By.xpath("//*[contains(text(),'Senator') or contains(text(),'Representative')]"));
                        if (!roleVec.isEmpty()) {
                            String txt = roleVec.get(0).getText().trim();
                            if (txt.toLowerCase().startsWith("senator")) title = "Senator";
                            else if (txt.toLowerCase().startsWith("representative")) title = "Representative";
                            else title = txt;
                        }
                    } catch (Exception ignored) {}
                    if (title.isEmpty()) title = "Senator";

                    // Position (District)
                    String position = "";
                    try {
                        WebElement d = driver.findElement(By.xpath("//*[contains(text(),'District')]"));
                        String txt = d.getText();
                        int idx = txt.indexOf(":");
                        if (idx >= 0) position = txt.substring(idx + 1).trim();
                    } catch (Exception ignored) {}

                    // Party
                    String party = "";
                    try {
                        WebElement p = driver.findElement(By.xpath("//*[contains(text(),'Party')]"));
                        String txt = p.getText();
                        int idx = txt.indexOf(":");
                        if (idx >= 0) party = txt.substring(idx + 1).trim();
                    } catch (Exception ignored) {}

                    // Email
                    String email = "";
                    try {
                        WebElement mail = driver.findElement(By.xpath("//a[starts-with(@href,'mailto:')]"));
                        String hrefMail = mail.getAttribute("href");
                        if (hrefMail != null && hrefMail.startsWith("mailto:")) {
                            email = hrefMail.substring("mailto:".length()).trim();
                        } else {
                            email = mail.getText().trim();
                        }
                    } catch (Exception ignored) {}

                    // Contact: prefer Session Contact then Interim Contact
                    String address = "";
                    String phone = "";
                    try {
                        int pos = bodyText.indexOf("Session Contact");
                        if (pos >= 0) {
                            String after = bodyText.substring(pos);
                            int endPos = after.indexOf("Interim Contact");
                            if (endPos < 0) endPos = Math.min(after.length(), 400);
                            String block = after.substring(0, endPos);
                            String[] lines = block.split("\\n");
                            StringBuilder addrB = new StringBuilder();
                            for (String ln : lines) {
                                ln = ln.trim();
                                if (ln.startsWith("Phone:")) {
                                    phone = ln.replace("Phone:","").trim();
                                } else if (!ln.isEmpty() && !ln.equals("Session Contact") && !ln.startsWith("Toll-Free:") && !ln.startsWith("Email:")) {
                                    addrB.append(ln).append(", ");
                                }
                            }
                            address = addrB.toString().replaceAll(", $", "");
                        }
                    } catch (Exception ignored) {}

                    if ((address == null || address.isEmpty()) || (phone == null || phone.isEmpty())) {
                        try {
                            int pos = bodyText.indexOf("Interim Contact");
                            if (pos >= 0) {
                                String after = bodyText.substring(pos);
                                int endPos = Math.min(after.length(), 400);
                                String block = after.substring(0, endPos);
                                String[] lines = block.split("\\n");
                                StringBuilder addrB = new StringBuilder();
                                for (String ln : lines) {
                                    ln = ln.trim();
                                    if (ln.startsWith("Phone:")) {
                                        if (phone == null || phone.isEmpty()) phone = ln.replace("Phone:","").trim();
                                    } else if (!ln.isEmpty() && !ln.equals("Interim Contact")) {
                                        addrB.append(ln).append(", ");
                                    }
                                }
                                if (address == null || address.isEmpty()) address = addrB.toString().replaceAll(", $", "");
                            }
                        } catch (Exception ignored) {}
                    }

                    Member m = new Member();
                    m.Name = name != null ? name : "";
                    m.Title = title != null ? title : "Senator";
                    m.Position = position != null ? position : "";
                    m.Party = party != null ? party : "";
                    m.Address = address != null ? address : "";
                    m.Phone = phone != null ? phone : "";
                    m.Email = email != null ? email : "";
                    m.URL = href;

                    members.add(m);
                    System.out.println("Scraped: " + m.Name + " | " + m.Party + " | " + m.URL);

                    // polite delay
                    try { Thread.sleep(700); } catch (InterruptedException ignored) {}

                } catch (Exception e) {
                    System.err.println("Failed to scrape: " + href + " -> " + e.getMessage());
                }
            }

            ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
            File out = new File("senators.json");
            mapper.writeValue(out, members);
            System.out.println("Wrote " + members.size() + " members to " + out.getAbsolutePath());

        } finally {
            driver.quit();
        }
    }
}
