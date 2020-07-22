// Shader created with Shader Forge v1.38 
// Shader Forge (c) Neat Corporation / Joachim Holmer - http://www.acegikmo.com/shaderforge/
// Note: Manually altering this data may prevent you from opening it in Shader Forge
/*SF_DATA;ver:1.38;sub:START;pass:START;ps:flbk:,iptp:0,cusa:False,bamd:0,cgin:,lico:1,lgpr:1,limd:0,spmd:1,trmd:0,grmd:0,uamb:True,mssp:True,bkdf:False,hqlp:False,rprd:False,enco:False,rmgx:True,imps:True,rpth:0,vtps:0,hqsc:True,nrmq:1,nrsp:0,vomd:0,spxs:False,tesm:0,olmd:1,culm:2,bsrc:0,bdst:0,dpts:2,wrdp:False,dith:0,atcv:False,rfrpo:True,rfrpn:Refraction,coma:15,ufog:True,aust:True,igpj:True,qofs:0,qpre:3,rntp:2,fgom:False,fgoc:True,fgod:False,fgor:False,fgmd:0,fgcr:0,fgcg:0,fgcb:0,fgca:1,fgde:0.01,fgrn:0,fgrf:300,stcl:False,atwp:False,stva:128,stmr:255,stmw:255,stcp:6,stps:0,stfa:0,stfz:0,ofsf:0,ofsu:0,f2p0:False,fnsp:True,fnfb:True,fsmp:False;n:type:ShaderForge.SFN_Final,id:4795,x:34219,y:32867,varname:node_4795,prsc:2|emission-598-OUT;n:type:ShaderForge.SFN_Tex2d,id:6074,x:32112,y:32806,varname:_MainTex,prsc:2,ntxv:0,isnm:False|UVIN-6399-OUT,TEX-6606-TEX;n:type:ShaderForge.SFN_Multiply,id:2393,x:33539,y:33002,varname:node_2393,prsc:2|A-7070-OUT,B-797-RGB,C-2053-A,D-3420-OUT,E-8084-OUT;n:type:ShaderForge.SFN_VertexColor,id:2053,x:33128,y:33162,varname:node_2053,prsc:2;n:type:ShaderForge.SFN_Color,id:797,x:33128,y:33014,ptovrint:True,ptlb:Color,ptin:_TintColor,varname:_TintColor,prsc:2,glob:False,taghide:False,taghdr:False,tagprd:False,tagnsco:False,tagnrm:False,c1:0.5,c2:0.5,c3:0.5,c4:1;n:type:ShaderForge.SFN_Slider,id:3420,x:31963,y:33225,ptovrint:False,ptlb:Final Power,ptin:_FinalPower,varname:node_3420,prsc:2,glob:False,taghide:False,taghdr:False,tagprd:False,tagnsco:False,tagnrm:False,min:0,cur:0,max:4;n:type:ShaderForge.SFN_TexCoord,id:3908,x:30857,y:32594,varname:node_3908,prsc:2,uv:0,uaff:False;n:type:ShaderForge.SFN_Add,id:6399,x:31197,y:32594,varname:node_6399,prsc:2|A-4062-UVOUT,B-5766-OUT;n:type:ShaderForge.SFN_Panner,id:4062,x:31023,y:32594,varname:node_4062,prsc:2,spu:0,spv:0.5|UVIN-3908-UVOUT;n:type:ShaderForge.SFN_Tex2d,id:1761,x:30382,y:32785,ptovrint:False,ptlb:Distortion,ptin:_Distortion,varname:node_1761,prsc:2,glob:False,taghide:False,taghdr:False,tagprd:False,tagnsco:False,tagnrm:False,ntxv:0,isnm:False;n:type:ShaderForge.SFN_Multiply,id:5766,x:30590,y:32871,varname:node_5766,prsc:2|A-1761-R,B-4318-OUT;n:type:ShaderForge.SFN_Slider,id:4318,x:30225,y:32969,ptovrint:False,ptlb:Distortion Power,ptin:_DistortionPower,varname:node_4318,prsc:2,glob:False,taghide:False,taghdr:False,tagprd:False,tagnsco:False,tagnrm:False,min:0,cur:0,max:0.4;n:type:ShaderForge.SFN_Tex2dAsset,id:6606,x:31769,y:32561,ptovrint:False,ptlb:Tex 01,ptin:_Tex01,varname:node_6606,glob:False,taghide:False,taghdr:False,tagprd:False,tagnsco:False,tagnrm:False,ntxv:0,isnm:False;n:type:ShaderForge.SFN_Tex2d,id:1726,x:32109,y:33069,varname:node_1726,prsc:2,ntxv:0,isnm:False|UVIN-8173-OUT,TEX-5449-TEX;n:type:ShaderForge.SFN_Multiply,id:6454,x:32489,y:32972,varname:node_6454,prsc:2|A-9363-OUT,B-9808-OUT;n:type:ShaderForge.SFN_TexCoord,id:293,x:30880,y:33049,varname:node_293,prsc:2,uv:0,uaff:False;n:type:ShaderForge.SFN_Panner,id:5731,x:31046,y:33049,varname:node_5731,prsc:2,spu:0,spv:1|UVIN-293-UVOUT;n:type:ShaderForge.SFN_Clamp01,id:7070,x:32976,y:32970,varname:node_7070,prsc:2|IN-6808-OUT;n:type:ShaderForge.SFN_Add,id:8173,x:31219,y:33049,varname:node_8173,prsc:2|A-5731-UVOUT,B-5766-OUT;n:type:ShaderForge.SFN_RemapRange,id:6808,x:32653,y:32970,varname:node_6808,prsc:2,frmn:0,frmx:1,tomn:-0.2,tomx:1|IN-6454-OUT;n:type:ShaderForge.SFN_Multiply,id:3025,x:32819,y:33005,varname:node_3025,prsc:2|A-6808-OUT,B-3420-OUT;n:type:ShaderForge.SFN_Tex2dAsset,id:5449,x:31757,y:32959,ptovrint:False,ptlb:Tex 02,ptin:_Tex02,varname:node_5449,glob:False,taghide:False,taghdr:False,tagprd:False,tagnsco:False,tagnrm:False,ntxv:0,isnm:False;n:type:ShaderForge.SFN_Multiply,id:9363,x:32322,y:32845,varname:node_9363,prsc:2|A-6074-R,B-7910-OUT;n:type:ShaderForge.SFN_Vector1,id:7910,x:32109,y:32965,varname:node_7910,prsc:2,v1:2;n:type:ShaderForge.SFN_Multiply,id:9808,x:32322,y:33028,varname:node_9808,prsc:2|A-7910-OUT,B-1726-R;n:type:ShaderForge.SFN_Add,id:8812,x:33128,y:33366,varname:node_8812,prsc:2|A-2932-OUT,B-8338-R;n:type:ShaderForge.SFN_Vector1,id:2932,x:32866,y:33319,varname:node_2932,prsc:2,v1:0.5;n:type:ShaderForge.SFN_TexCoord,id:1483,x:32419,y:33430,varname:node_1483,prsc:2,uv:0,uaff:False;n:type:ShaderForge.SFN_Panner,id:2010,x:32592,y:33430,varname:node_2010,prsc:2,spu:0,spv:5|UVIN-1483-UVOUT;n:type:ShaderForge.SFN_Tex2dAsset,id:1718,x:32455,y:33701,ptovrint:False,ptlb:Tex 03,ptin:_Tex03,varname:node_1718,glob:False,taghide:False,taghdr:False,tagprd:False,tagnsco:False,tagnrm:False,ntxv:0,isnm:False;n:type:ShaderForge.SFN_Tex2d,id:8338,x:32789,y:33430,varname:node_8338,prsc:2,ntxv:0,isnm:False|UVIN-2010-UVOUT,TEX-1718-TEX;n:type:ShaderForge.SFN_Clamp01,id:8084,x:33296,y:33366,varname:node_8084,prsc:2|IN-8812-OUT;n:type:ShaderForge.SFN_Clamp01,id:901,x:33727,y:33002,varname:node_901,prsc:2|IN-2393-OUT;n:type:ShaderForge.SFN_Multiply,id:598,x:33913,y:33002,varname:node_598,prsc:2|A-901-OUT,B-2053-A;proporder:797-3420-1761-4318-6606-5449-1718;pass:END;sub:END;*/

Shader "SineVFX/MeshPacks/PortalOpenVFX03" {
    Properties {
        _TintColor ("Color", Color) = (0.5,0.5,0.5,1)
        _FinalPower ("Final Power", Range(0, 4)) = 0
        _Distortion ("Distortion", 2D) = "white" {}
        _DistortionPower ("Distortion Power", Range(0, 0.4)) = 0
        _Tex01 ("Tex 01", 2D) = "white" {}
        _Tex02 ("Tex 02", 2D) = "white" {}
        _Tex03 ("Tex 03", 2D) = "white" {}
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
            Blend One One
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
            uniform float4 _TintColor;
            uniform float _FinalPower;
            uniform sampler2D _Distortion; uniform float4 _Distortion_ST;
            uniform float _DistortionPower;
            uniform sampler2D _Tex01; uniform float4 _Tex01_ST;
            uniform sampler2D _Tex02; uniform float4 _Tex02_ST;
            uniform sampler2D _Tex03; uniform float4 _Tex03_ST;
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
                float4 node_9121 = _Time;
                float4 _Distortion_var = tex2D(_Distortion,TRANSFORM_TEX(i.uv0, _Distortion));
                float node_5766 = (_Distortion_var.r*_DistortionPower);
                float2 node_6399 = ((i.uv0+node_9121.g*float2(0,0.5))+node_5766);
                float4 _MainTex = tex2D(_Tex01,TRANSFORM_TEX(node_6399, _Tex01));
                float node_7910 = 2.0;
                float2 node_8173 = ((i.uv0+node_9121.g*float2(0,1))+node_5766);
                float4 node_1726 = tex2D(_Tex02,TRANSFORM_TEX(node_8173, _Tex02));
                float node_6808 = (((_MainTex.r*node_7910)*(node_7910*node_1726.r))*1.2+-0.2);
                float2 node_2010 = (i.uv0+node_9121.g*float2(0,5));
                float4 node_8338 = tex2D(_Tex03,TRANSFORM_TEX(node_2010, _Tex03));
                float3 emissive = (saturate((saturate(node_6808)*_TintColor.rgb*i.vertexColor.a*_FinalPower*saturate((0.5+node_8338.r))))*i.vertexColor.a);
                float3 finalColor = emissive;
                fixed4 finalRGBA = fixed4(finalColor,1);
                UNITY_APPLY_FOG_COLOR(i.fogCoord, finalRGBA, fixed4(0,0,0,1));
                return finalRGBA;
            }
            ENDCG
        }
    }
    CustomEditor "ShaderForgeMaterialInspector"
}
