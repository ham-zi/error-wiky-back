package com.errorwiky.ai;
import com.errorwiky.post.ErrorCategory;
public record AiRecommendResponse(String title,ErrorCategory category,String source,String notice){}
