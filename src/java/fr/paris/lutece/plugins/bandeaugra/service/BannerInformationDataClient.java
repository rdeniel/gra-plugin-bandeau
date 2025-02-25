/*
 * Copyright (c) 2002-2017, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.bandeaugra.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import fr.paris.lutece.plugins.bandeaugra.rs.BannerInformationsRest;
import fr.paris.lutece.plugins.bandeaugra.rs.Constants;
import fr.paris.lutece.plugins.mylutece.modules.oauth2.service.Oauth2Service;
import fr.paris.lutece.plugins.oauth2.business.Token;
import fr.paris.lutece.plugins.oauth2.dataclient.AbstractDataClient;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.SecurityService;
import fr.paris.lutece.portal.service.util.AppLogService;

/**
 * UserInfoDataClient
 */
public class BannerInformationDataClient extends AbstractDataClient
{
    private static ObjectMapper _mapper;

    static
    {
        _mapper = new ObjectMapper(  );
        _mapper.configure( DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false );
    }

    /**
     * {@inheritDoc }
     */
    public void handleToken( Token token , HttpServletRequest  request , HttpServletResponse  response )
    {
        
        String strHeaderOrigin=request.getHeader( Constants.HEADER_ORIGIN );
        String strAccessControlAllowOrigin=strHeaderOrigin;
       
        try
        {
            Map<String,Object> mapUserInfo = parse( getData( token ) );
            Oauth2Service.getInstance().processAuthentication( request, mapUserInfo, token );
            
            LuteceUser user = SecurityService.getInstance(  ).getRegisteredUser( request );
            
            byte[] strHeadbandInformationsJson=BannerInformationsRest.getJsonBannerInformations( user ) .getBytes("UTF-8");
            
            response.setHeader( Constants.ACCESS_CONTROL_ALLOW_ORIGIN, strAccessControlAllowOrigin );
            response.setHeader( Constants.ACCESS_CONTROL_ALLOW_CREDENTIALS, Constants.METHODS_LIST );
            response.setHeader( Constants.ACCESS_CONTROL_ALLOW_METHODS, Boolean.TRUE.toString( ) );
            response.setContentType( "application/json" );
            response.setContentLength( strHeadbandInformationsJson.length );
            response.getOutputStream( ).write( strHeadbandInformationsJson ) ; 
        }
        catch ( IOException ex )
        {
            AppLogService.error( "Error parsing UserInfo ", ex );
        }
    }

    /**
     * parse the JSON for a token
     * @param strJson The JSON
     * @return The UserInfo
     * @throws java.io.IOException if an error occurs
     */
    Map<String,Object> parse( String strJson ) throws IOException
    {
    	TypeReference<HashMap<String, Object>> typeRef 
    	  = new TypeReference<HashMap<String, Object>>( ) {};
    	
    	return  _mapper.readValue( strJson, typeRef );
    }
}
