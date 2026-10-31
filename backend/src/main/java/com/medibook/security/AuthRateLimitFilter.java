package com.medibook.security;
import jakarta.servlet.*;import jakarta.servlet.http.*;import org.springframework.http.MediaType;import org.springframework.stereotype.Component;import org.springframework.web.filter.OncePerRequestFilter;import java.io.IOException;import java.time.Instant;import java.util.concurrent.ConcurrentHashMap;import java.util.concurrent.atomic.AtomicInteger;
@Component public class AuthRateLimitFilter extends OncePerRequestFilter {
 private record Window(long minute,AtomicInteger count){}
 private final ConcurrentHashMap<String,Window> windows=new ConcurrentHashMap<>();
 @Override protected boolean shouldNotFilter(HttpServletRequest request){String p=request.getServletPath();return !((p.equals("/api/auth/login")||p.equals("/api/auth/register"))&&request.getMethod().equals("POST"));}
 @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{long minute=Instant.now().getEpochSecond()/60;String key=req.getRemoteAddr()+":"+req.getServletPath();Window w=windows.compute(key,(k,old)->old==null||old.minute()!=minute?new Window(minute,new AtomicInteger()):old);int limit=req.getServletPath().endsWith("/register")?8:15;if(w.count().incrementAndGet()>limit){res.setStatus(429);res.setContentType(MediaType.APPLICATION_JSON_VALUE);res.getWriter().write("{\"status\":429,\"error\":\"Too many requests\",\"message\":\"Please wait a minute before trying again.\"}");return;}if(windows.size()>10000)windows.entrySet().removeIf(e->e.getValue().minute()<minute-2);chain.doFilter(req,res);}
}
