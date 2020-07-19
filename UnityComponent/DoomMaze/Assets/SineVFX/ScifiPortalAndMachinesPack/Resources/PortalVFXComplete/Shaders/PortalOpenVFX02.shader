// Shader created with Shader Forge v1.38 
// Shader Forge (c) Neat Corporation / Joachim Holmer - http://www.acegikmo.com/shaderforge/
// Note: Manually altering this data may prevent you from opening it in Shader Forge
/*SF_DATA;ver:1.38;sub:START;pass:START;ps:flbk:,iptp:0,cusa:False,bamd:0,cgin:,lico:1,lgpr:1,limd:0,spmd:1,trmd:0,grmd:0,uamb:True,mssp:True,bkdf:False,hqlp:False,rprd:False,enco:False,rmgx:True,imps:True,rpth:0,vtps:0,hqsc:True,nrmq:1,nrsp:0,vomd:0,spxs:False,tesm:0,olmd:1,culm:2,bsrc:3,bdst:7,dpts:2,wrdp:False,dith:0,atcv:False,rfrpo:True,rfrpn:Refraction,coma:15,ufog:True,aust:True,igpj:True,qofs:0,qpre:3,rntp:2,fgom:False,fgoc:False,fgod:False,fgor:False,fgmd:0,fgcr:0,fgcg:0,fgcb:0,fgca:1,fgde:0.01,fgrn:0,fgrf:300,stcl:False,atwp:False,stva:128,stmr:255,stmw:255,stcp:6,stps:0,stfa:0,stfz:0,ofsf:0,ofsu:0,f2p0:False,fnsp:True,fnfb:True,fsmp:False;n:type:ShaderForge.SFN_Final,id:4795,x:33665,y:32609,varname:node_4795,prsc:2|emission-7411-OUT,alpha-5061-OUT;n:type:ShaderForge.SFN_Tex2d,id:6074,x:32753,y:32562,ptovrint:False,ptlb:MainTex,ptin:_MainTex,varname:_MainTex,prsc:2,glob:False,taghide:False,taghdr:False,tagprd:False,tagnsco:False,tagnrm:False,ntxv:0,isnm:False|UVIN-278-OUT;n:type:ShaderForge.SFN_VertexColor,id:2053,x:31076,y:33438,varname:node_2053,prsc:2;n:type:ShaderForge.SFN_TexCoord,id:8330,x:31886,y:32666,varname:node_8330,prsc:2,uv:0,uaff:False;n:type:ShaderForge.SFN_Add,id:278,x:32487,y:32600,varname:node_278,prsc:2|A-8330-UVOUT,B-7503-OUT;n:type:ShaderForge.SFN_Tex2dAsset,id:922,x:30968,y:32777,ptovrint:False,ptlb:Tex 01,ptin:_Tex01,varname:node_922,glob:False,taghide:False,taghdr:False,tagprd:False,tagnsco:False,tagnrm:False,ntxv:0,isnm:False;n:type:ShaderForge.SFN_Tex2d,id:5965,x:31382,y:32644,varname:node_5965,prsc:2,ntxv:0,isnm:False|UVIN-1652-UVOUT,TEX-922-TEX;n:type:ShaderForge.SFN_Tex2d,id:972,x:31382,y:32848,varname:node_972,prsc:2,ntxv:0,isnm:False|UVIN-7982-OUT,TEX-922-TEX;n:type:ShaderForge.SFN_TexCoord,id:2909,x:30699,y:32536,varname:node_2909,prsc:2,uv:0,uaff:False;n:type:ShaderForge.SFN_Panner,id:1652,x:30863,y:32536,varname:node_1652,prsc:2,spu:0.1,spv:0.1|UVIN-2909-UVOUT;n:type:ShaderForge.SFN_Panner,id:6454,x:30883,y:32957,varname:node_6454,prsc:2,spu:-0.1,spv:-0.1|UVIN-8978-UVOUT;n:type:ShaderForge.SFN_TexCoord,id:8978,x:30719,y:32957,varname:node_8978,prsc:2,uv:0,uaff:False;n:type:ShaderForge.SFN_Multiply,id:7982,x:31057,y:33002,varname:node_7982,prsc:2|A-6454-UVOUT,B-5130-OUT;n:type:ShaderForge.SFN_Vector1,id:5130,x:30883,y:33112,varname:node_5130,prsc:2,v1:1.25;n:type:ShaderForge.SFN_Slider,id:5838,x:31225,y:33002,ptovrint:False,ptlb:Distortion Power,ptin:_DistortionPower,varname:node_5838,prsc:2,glob:False,taghide:False,taghdr:False,tagprd:False,tagnsco:False,tagnrm:False,min:0,cur:0,max:1;n:type:ShaderForge.SFN_Add,id:7411,x:33455,y:32703,varname:node_7411,prsc:2|A-6074-RGB,B-3396-OUT,C-6004-OUT;n:type:ShaderForge.SFN_Tex2d,id:2414,x:32762,y:32885,ptovrint:False,ptlb:Circle,ptin:_Circle,varname:node_2414,prsc:2,glob:False,taghide:False,taghdr:False,tagprd:False,tagnsco:False,tagnrm:False,ntxv:0,isnm:False|UVIN-6181-OUT;n:type:ShaderForge.SFN_Multiply,id:3396,x:32945,y:32965,varname:node_3396,prsc:2|A-2414-R,B-9708-RGB;n:type:ShaderForge.SFN_Color,id:9708,x:32762,y:33073,ptovrint:False,ptlb:Circle Color,ptin:_CircleColor,varname:node_9708,prsc:2,glob:False,taghide:False,taghdr:False,tagprd:False,tagnsco:False,tagnrm:False,c1:0.5,c2:0.5,c3:0.5,c4:1;n:type:ShaderForge.SFN_Multiply,id:5443,x:31635,y:32772,varname:node_5443,prsc:2|A-5965-R,B-972-R,C-2053-R;n:type:ShaderForge.SFN_Slider,id:5197,x:31225,y:33113,ptovrint:False,ptlb:Distortion Power Circle,ptin:_DistortionPowerCircle,varname:_DistortionPower_copy,prsc:2,glob:False,taghide:False,taghdr:False,tagprd:False,tagnsco:False,tagnrm:False,min:0,cur:0,max:1;n:type:ShaderForge.SFN_Multiply,id:7503,x:31886,y:32812,varname:node_7503,prsc:2|A-5443-OUT,B-5838-OUT;n:type:ShaderForge.SFN_Multiply,id:5780,x:31910,y:33073,varname:node_5780,prsc:2|A-5443-OUT,B-5197-OUT,C-5846-OUT,D-9812-OUT;n:type:ShaderForge.SFN_Add,id:6181,x:32466,y:32976,varname:node_6181,prsc:2|A-8330-UVOUT,B-5780-OUT;n:type:ShaderForge.SFN_TexCoord,id:8240,x:31364,y:33233,varname:node_8240,prsc:2,uv:0,uaff:False;n:type:ShaderForge.SFN_Normalize,id:5846,x:31693,y:33233,varname:node_5846,prsc:2|IN-8444-OUT;n:type:ShaderForge.SFN_RemapRange,id:8444,x:31524,y:33233,varname:node_8444,prsc:2,frmn:0,frmx:1,tomn:-1,tomx:1|IN-8240-UVOUT;n:type:ShaderForge.SFN_OneMinus,id:9812,x:31498,y:33442,varname:node_9812,prsc:2|IN-2053-G;n:type:ShaderForge.SFN_Tex2d,id:9853,x:31857,y:33891,ptovrint:False,ptlb:Open Mask,ptin:_OpenMask,varname:node_9853,prsc:2,glob:False,taghide:False,taghdr:False,tagprd:False,tagnsco:False,tagnrm:False,ntxv:0,isnm:False|UVIN-7657-OUT;n:type:ShaderForge.SFN_TexCoord,id:6546,x:30663,y:33996,varname:node_6546,prsc:2,uv:0,uaff:False;n:type:ShaderForge.SFN_RemapRange,id:6825,x:30837,y:33996,varname:node_6825,prsc:2,frmn:0,frmx:1,tomn:-0.5,tomx:0.5|IN-6546-UVOUT;n:type:ShaderForge.SFN_Multiply,id:4012,x:31038,y:33996,varname:node_4012,prsc:2|A-6825-OUT,B-1775-OUT;n:type:ShaderForge.SFN_Slider,id:9775,x:30344,y:34446,ptovrint:False,ptlb:Open Mask Scale,ptin:_OpenMaskScale,varname:node_9775,prsc:2,glob:False,taghide:False,taghdr:False,tagprd:False,tagnsco:False,tagnrm:False,min:0,cur:0,max:4;n:type:ShaderForge.SFN_Multiply,id:5061,x:32547,y:33281,varname:node_5061,prsc:2|A-2053-A,B-7950-OUT;n:type:ShaderForge.SFN_Add,id:816,x:31227,y:34051,varname:node_816,prsc:2|A-4012-OUT,B-3864-OUT;n:type:ShaderForge.SFN_Vector2,id:3864,x:31038,y:34130,varname:node_3864,prsc:2,v1:0.5,v2:0.5;n:type:ShaderForge.SFN_Normalize,id:826,x:31256,y:33814,varname:node_826,prsc:2|IN-6825-OUT;n:type:ShaderForge.SFN_Multiply,id:8137,x:31443,y:33814,varname:node_8137,prsc:2|A-826-OUT,B-3011-OUT,C-1068-OUT;n:type:ShaderForge.SFN_Add,id:7657,x:31619,y:33927,varname:node_7657,prsc:2|A-8137-OUT,B-816-OUT;n:type:ShaderForge.SFN_Slider,id:3011,x:30655,y:33860,ptovrint:False,ptlb:Open Mask Distortion Power,ptin:_OpenMaskDistortionPower,varname:node_3011,prsc:2,glob:False,taghide:False,taghdr:False,tagprd:False,tagnsco:False,tagnrm:False,min:0,cur:0,max:10;n:type:ShaderForge.SFN_Multiply,id:6004,x:33053,y:33271,varname:node_6004,prsc:2|A-9708-RGB,B-5091-OUT;n:type:ShaderForge.SFN_Vector1,id:3967,x:31857,y:34054,varname:node_3967,prsc:2,v1:3;n:type:ShaderForge.SFN_Add,id:5091,x:32447,y:34226,varname:node_5091,prsc:2|A-8315-OUT,B-6459-OUT;n:type:ShaderForge.SFN_RemapRange,id:8424,x:31467,y:34487,varname:node_8424,prsc:2,frmn:0,frmx:4,tomn:-1,tomx:1|IN-1775-OUT;n:type:ShaderForge.SFN_Clamp01,id:6459,x:31664,y:34487,varname:node_6459,prsc:2|IN-8424-OUT;n:type:ShaderForge.SFN_Add,id:3791,x:32077,y:33776,varname:node_3791,prsc:2|A-9853-R,B-9853-G;n:type:ShaderForge.SFN_Clamp01,id:7950,x:32243,y:33776,varname:node_7950,prsc:2|IN-3791-OUT;n:type:ShaderForge.SFN_Get,id:1068,x:31235,y:33940,varname:node_1068,prsc:2|IN-9432-OUT;n:type:ShaderForge.SFN_Set,id:9432,x:31886,y:32605,varname:NoiseFull,prsc:2|IN-5443-OUT;n:type:ShaderForge.SFN_Multiply,id:8315,x:32134,y:33982,varname:node_8315,prsc:2|A-9853-G,B-3967-OUT,C-1775-OUT;n:type:ShaderForge.SFN_Exp,id:2753,x:30709,y:34442,varname:node_2753,prsc:2,et:1|IN-9775-OUT;n:type:ShaderForge.SFN_Subtract,id:1775,x:30924,y:34466,varname:node_1775,prsc:2|A-2753-OUT,B-4652-OUT;n:type:ShaderForge.SFN_Vector1,id:4652,x:30709,y:34588,varname:node_4652,prsc:2,v1:1;proporder:6074-922-5838-2414-9708-5197-9853-9775-3011;pass:END;sub:END;*/

Shader "SineVFX/MeshPacks/PortalOpenVFX02" {
    Properties {
        _MainTex ("MainTex", 2D) = "white" {}
        _Tex01 ("Tex 01", 2D) = "white" {}
        _DistortionPower ("Distortion Power", Range(0, 1)) = 0
        _Circle ("Circle", 2D) = "white" {}
        _CircleColor ("Circle Color", Color) = (0.5,0.5,0.5,1)
        _DistortionPowerCircle ("Distortion Power Circle", Range(0, 1)) = 0
        _OpenMask ("Open Mask", 2D) = "white" {}
        _OpenMaskScale ("Open Mask Scale", Range(0, 4)) = 0
        _OpenMaskDistortionPower ("Open Mask Distortion Power", Range(0, 10)) = 0
        [HideInInspector]_Cutoff ("Alpha cutoff", Range(0,1)) = 0.5
    }
    SubShader {
        Tags {
            "IgnoreProjector"="True"
            "Queue"="Transparent"
            "RenderType"="Transparent"
        }
        Pass {
            Name "FORWARD"
            Tags {
                "LightMode"="ForwardBase"
            }
            Blend SrcAlpha OneMinusSrcAlpha
            Cull Off
            ZWrite Off
            
            CGPROGRAM
            #pragma vertex vert
            #pragma fragment frag
            #define UNITY_PASS_FORWARDBASE
            #include "UnityCG.cginc"
            #pragma multi_compile_fwdbase
            #pragma multi_compile_fog
            #pragma only_renderers d3d9 d3d11 glcore gles gles3 metal d3d11_9x xboxone ps4 psp2 n3ds wiiu 
            #pragma target 3.0
            uniform sampler2D _MainTex; uniform float4 _MainTex_ST;
            uniform sampler2D _Tex01; uniform float4 _Tex01_ST;
            uniform float _DistortionPower;
            uniform sampler2D _Circle; uniform float4 _Circle_ST;
            uniform float4 _CircleColor;
            uniform float _DistortionPowerCircle;
            uniform sampler2D _OpenMask; uniform float4 _OpenMask_ST;
            uniform float _OpenMaskScale;
            uniform float _OpenMaskDistortionPower;
            struct VertexInput {
                float4 vertex : POSITION;
                float2 texcoord0 : TEXCOORD0;
                float4 vertexColor : COLOR;
            };
            struct VertexOutput {
                float4 pos : SV_POSITION;
                float2 uv0 : TEXCOORD0;
                float4 vertexColor : COLOR;
                UNITY_FOG_COORDS(1)
            };
            VertexOutput vert (VertexInput v) {
                VertexOutput o = (VertexOutput)0;
                o.uv0 = v.texcoord0;
                o.vertexColor = v.vertexColor;
                o.pos = UnityObjectToClipPos( v.vertex );
                UNITY_TRANSFER_FOG(o,o.pos);
                return o;
            }
            float4 frag(VertexOutput i, float facing : VFACE) : COLOR {
                float isFrontFace = ( facing >= 0 ? 1 : 0 );
                float faceSign = ( facing >= 0 ? 1 : -1 );
////// Lighting:
////// Emissive:
                float4 node_8236 = _Time;
                float2 node_1652 = (i.uv0+node_8236.g*float2(0.1,0.1));
                float4 node_5965 = tex2D(_Tex01,TRANSFORM_TEX(node_1652, _Tex01));
                float2 node_7982 = ((i.uv0+node_8236.g*float2(-0.1,-0.1))*1.25);
                float4 node_972 = tex2D(_Tex01,TRANSFORM_TEX(node_7982, _Tex01));
                float node_5443 = (node_5965.r*node_972.r*i.vertexColor.r);
                float2 node_278 = (i.uv0+(node_5443*_DistortionPower));
                float4 _MainTex_var = tex2D(_MainTex,TRANSFORM_TEX(node_278, _MainTex));
                float2 node_6181 = (i.uv0+(node_5443*_DistortionPowerCircle*normalize((i.uv0*2.0+-1.0))*(1.0 - i.vertexColor.g)));
                float4 _Circle_var = tex2D(_Circle,TRANSFORM_TEX(node_6181, _Circle));
                float2 node_6825 = (i.uv0*1.0+-0.5);
                float NoiseFull = node_5443;
                float node_1775 = (exp2(_OpenMaskScale)-1.0);
                float2 node_7657 = ((normalize(node_6825)*_OpenMaskDistortionPower*NoiseFull)+((node_6825*node_1775)+float2(0.5,0.5)));
                float4 _OpenMask_var = tex2D(_OpenMask,TRANSFORM_TEX(node_7657, _OpenMask));
                float3 emissive = (_MainTex_var.rgb+(_Circle_var.r*_CircleColor.rgb)+(_CircleColor.rgb*((_OpenMask_var.g*3.0*node_1775)+saturate((node_1775*0.5+-1.0)))));
                float3 finalColor = emissive;
                fixed4 finalRGBA = fixed4(finalColor,(i.vertexColor.a*saturate((_OpenMask_var.r+_OpenMask_var.g))));
                UNITY_APPLY_FOG(i.fogCoord, finalRGBA);
                return finalRGBA;
            }
            ENDCG
        }
    }
    CustomEditor "ShaderForgeMaterialInspector"
}
