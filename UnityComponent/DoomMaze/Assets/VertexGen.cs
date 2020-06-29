using UnityEngine;


public class VertexGen : MonoBehaviour
{
    public GameObject vertex;
    public GameObject board;

    // Start is called before the first frame update
    void Start()
    {
        //get board object from scene
        //board = GameObject.Find("Board");
        float _width = board.GetComponent<RectTransform>().rect.width;
        float _height = board.GetComponent<RectTransform>().rect.height;
        Debug.Log(_width);
        Debug.Log(_height);

        //top left
        float separation = 0.1f;
        int _x1; int _y1; int _x2; int _y2;
        _x1 = (int)(_width / (-2));
        _y1 = (int)(_height / (-2));
        _x2 = (int)(_width / (2));
        _y2 = (int)(_height / (2));

        for(float i = _x1; i <= _x2; i += separation)
        {
            for(float j = _y1; j <= _y2; j += separation)
            {
                GameObject new_vertex = Instantiate(vertex);
                new_vertex.transform.position = new Vector3(i, 0, j);
            }
        }


        //Debug.Log(_x1);
    }

    // Update is called once per frame
    void Update()
    {
        
    }
}
