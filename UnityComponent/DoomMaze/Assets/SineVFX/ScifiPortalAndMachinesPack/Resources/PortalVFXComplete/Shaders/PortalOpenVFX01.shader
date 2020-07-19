// Shader created with Shader Forge v1.38 
// Shader Forge (c) Neat Corporation / Joachim Holmer - http://www.acegikmo.com/shaderforge/
// Note: Manually altering this data may prevent you from opening it in Shader Forge
/*SF_DATA;ver:1.38;sub:START;pass:START;ps:flbk:,iptp:0,cusa:False,bamd:0,cgin:,lico:1,lgpr:1,limd:0,spmd:1,trmd:0,grmd:0,uamb:True,mssp:True,bkdf:False,hqlp:False,rprd:False,enco:False,rmgx:True,imps:True,rpth:0,vtps:0,hqsc:True,nrmq:1,nrsp:0,vomd:0,spxs:False,tesm:0,olmd:1,culm:2,bsrc:0,bdst:0,dpts:2,wrdp:False,dith:0,atcv:False,rfrpo:True,rfrpn:Refraction,coma:15,ufog:True,aust:True,igpj:True,qofs:0,qpre:3,rntp:2,fgom:False,fgoc:True,fgod:False,fgor:False,fgmd:0,fgcr:0,fgcg:0,fgcb:0,fgca:1,fgde:0.01,fgrn:0,fgrf:300,stcl:False,atwp:False,stva:128,stmr:255,stmw:255,stcp:6,stps:0,stfa:0,stfz:0,ofsf:0,ofsu:0,f2p0:False,fnsp:True,fnfb:True,fsmp:False;n:type:ShaderForge.SFN_Final,id:4795,x:33498,y:32700,varname:node_4795,prsc:2|emission-2393-OUT;n:type:ShaderForge.SFN_Tex2d,id:6074,x:31932,y:32809,varname:_MainTex,prsc:2,ntxv:0,isnm:False|UVIN-7555-OUT,TEX-6606-TEX;n:type:ShaderForge.SFN_Multiply,id:2393,x:33200,y:32940,varname:node_2393,prsc:2|A-7070-OUT,B-797-RGB,C-2053-A;n:type:ShaderForge.SFN_VertexColor,id:2053,x:32948,y:33165,varname:node_2053,prsc:2;n:type:ShaderForge.SFN_Color,id:797,x:32948,y:33017,ptovrint:True,ptlb:Color,ptin:_TintColor,varname:_TintColor,prsc:2,glob:False,taghide:False,taghdr:False,tagprd:False,tagnsco:False,tagnrm:False,c1:0.5,c2:0.5,c3:0.5,c4:1;n:type:ShaderForge.SFN_Slider,id:3420,x:32392,y:33101,ptovrint:False,ptlb:Final Power,ptin:_FinalPower,varname:node_3420,prsc:2,glob:False,taghide:False,taghdr:False,tagprd:False,tagnsco:False,tagnrm:False,min:0,cur:0,max:4;n:type:ShaderForge.SFN_TexCoord,id:3908,x:30857,y:32594,varname:node_3908,prsc:2,uv:0,uaff:False;n:type:ShaderForge.SFN_Add,id:6399,x:31391,y:32804,varname:node_6399,prsc:2|A-9891-G,B-5766-OUT;n:type:ShaderForge.SFN_Panner,id:4062,x:31023,y:32594,varname:node_4062,prsc:2,spu:0,spv:0.5|UVIN-3908-UVOUT;n:type:ShaderForge.SFN_Tex2d,id:1761,x:30344,y:33123,ptovrint:False,ptlb:Distortion,ptin:_Distortion,varname:node_1761,prsc:2,glob:False,taghide:False,taghdr:False,tagprd:False,tagnsco:False,tagnrm:False,ntxv:0,isnm:False;n:type:ShaderForge.SFN_Multiply,id:5766,x:30552,y:33209,varname:node_5766,prsc:2|A-1761-R,B-4318-OUT,C-704-OUT;n:type:ShaderForge.SFN_Slider,id:4318,x:30187,y:33307,ptovrint:False,ptlb:Distortion Power,ptin:_DistortionPower,varname:node_4318,prsc:2,glob:False,taghide:False,taghdr:False,tagprd:False,tagnsco:False,tagnrm:False,min:0,cur:0,max:5;n:type:ShaderForge.SFN_Add,id:7938,x:32392,y:32930,varname:node_7938,prsc:2|A-6074-R,B-6454-OUT;n:type:ShaderForge.SFN_Tex2dAsset,id:6606,x:31534,y:33258,ptovrint:False,ptlb:Tex 01,ptin:_Tex01,varname:node_6606,glob:False,taghide:False,taghdr:False,tagprd:False,tagnsco:False,tagnrm:False,ntxv:0,isnm:False;n:type:ShaderForge.SFN_Tex2d,id:1726,x:31929,y:33072,varname:node_1726,prsc:2,ntxv:0,isnm:False|UVIN-5731-UVOUT,TEX-6606-TEX;n:type:ShaderForge.SFN_Multiply,id:6454,x:32167,y:33153,varname:node_6454,prsc:2|A-1726-R,B-8182-R;n:type:ShaderForge.SFN_VertexColor,id:8182,x:31929,y:33212,varname:node_8182,prsc:2;n:type:ShaderForge.SFN_TexCoord,id:293,x:31152,y:33162,varname:node_293,prsc:2,uv:0,uaff:False;n:type:ShaderForge.SFN_Panner,id:5731,x:31318,y:33162,varname:node_5731,prsc:2,spu:0,spv:1|UVIN-293-UVOUT;n:type:ShaderForge.SFN_ComponentMask,id:9891,x:31190,y:32686,varname:node_9891,prsc:2,cc1:0,cc2:1,cc3:-1,cc4:-1|IN-4062-UVOUT;n:type:ShaderForge.SFN_Append,id:7555,x:31574,y:32709,varname:node_7555,prsc:2|A-9891-R,B-6399-OUT;n:type:ShaderForge.SFN_VertexColor,id:8495,x:30222,y:33404,varname:node_8495,prsc:2;n:type:ShaderForge.SFN_OneMinus,id:704,x:30420,y:33447,varname:node_704,prsc:2|IN-8495-A;n:type:ShaderForge.SFN_Clamp01,id:7070,x:32750,y:32929,varname:node_7070,prsc:2|IN-343-OUT;n:type:ShaderForge.SFN_Multiply,id:343,x:32565,y:32929,varname:node_343,prsc:2|A-7938-OUT,B-3420-OUT;proporder:797-3420-1761-4318-6606;pass:END;sub:END;*/

Shader "SineVFX/MeshPacks/PortalOpenVFX01" {
    Properties {
        _TintColor ("Color", Color) = (0.5,0.5,0.5,1)
        _FinalPower ("Final Power", Range(0, 4)) = 0
        _Distortion ("Distortion", 2D) = "white" {}
        _DistortionPower ("Distortion Power", Range(0, 5)) = 0
        _Tex01 ("Tex 01", 2D) = "white" {}
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
                float4 node_8533 = _Time;
                float2 node_9891 = (i.uv0+node_8533.g*float2(0,0.5)).rg;
                float4 _Distortion_var = tex2D(_Distortion,TRANSFORM_TEX(i.uv0, _Distortion));
                float2 node_7555 = float2(node_9891.r,(node_9891.g+(_Distortion_var.r*_DistortionPower*(1.0 - i.vertexColor.a))));
                float4 _MainTex = tex2D(_Tex01,TRANSFORM_TEX(node_7555, _Tex01));
                float2 node_5731 = (i.uv0+node_8533.g*float2(0,1));
                float4 node_1726 = tex2D(_Tex01,TRANSFORM_TEX(node_5731, _Tex01));
                float3 emissive = (saturate(((_MainTex.r+(node_1726.r*i.vertexColor.r))*_FinalPower))*_TintColor.rgb*i.vertexColor.a);
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
